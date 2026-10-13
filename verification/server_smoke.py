"""Start a loopback-only server, require readiness, then save/stop over local RCON."""
from pathlib import Path
import subprocess, threading, time, socket, struct, secrets, os, signal
root = Path(__file__).resolve().parents[1]
run = root / 'run'
run.mkdir(exist_ok=True)
(run / 'eula.txt').write_text('eula=true\n')
password = secrets.token_hex(24)
(run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25569\nonline-mode=false\nenable-rcon=true\nrcon.port=25578\nrcon.password=' + password + '\nbroadcast-rcon-to-ops=false\nview-distance=2\nsimulation-distance=2\npause-when-empty-seconds=-1\n')
log = root / 'verification/server-smoke.log'
ready, failure = False, False
stop_error = []
started = time.monotonic()
process = subprocess.Popen(['./gradlew', 'runServer', '--no-daemon', '--console=plain'], cwd=root, stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, bufsize=1, start_new_session=True)
def deadline():
    if process.poll() is None:
        try: os.killpg(process.pid, signal.SIGTERM)
        except ProcessLookupError: return
        time.sleep(5)
        if process.poll() is None:
            try: os.killpg(process.pid, signal.SIGKILL)
            except ProcessLookupError: pass
def read_exact(connection, length):
    data = b''
    while len(data) < length:
        part = connection.recv(length - len(data))
        if not part: raise EOFError('RCON connection closed')
        data += part
    return data
def receive(connection):
    length = struct.unpack('<i', read_exact(connection, 4))[0]
    if length < 10 or length > 8192: raise ValueError('Invalid RCON packet')
    body = read_exact(connection, length)
    return struct.unpack('<ii', body[:8]), body[8:-2]
def packet(connection, number, kind, text):
    body = struct.pack('<ii', number, kind) + text.encode() + b'\0\0'
    connection.sendall(struct.pack('<i', len(body)) + body)
def stop_server():
    try:
        with socket.create_connection(('127.0.0.1', 25578), timeout=15) as connection:
            connection.settimeout(15)
            packet(connection, 1, 3, password)
            header, _ = receive(connection)
            if header[0] == -1: raise ValueError('RCON authentication rejected')
            if header[1] != 2: header, _ = receive(connection)
            if header != (1, 2): raise ValueError('Unexpected RCON auth response')
            packet(connection, 2, 2, 'stop')
            try: receive(connection)
            except EOFError: pass
    except Exception as error:
        stop_error.append(type(error).__name__ + ': ' + str(error))
timer = threading.Timer(180, deadline)
timer.start()
stop_thread = None
try:
    with log.open('w') as output:
        for line in process.stdout:
            output.write(line); output.flush()
            print(line, end='', flush=True)
            if 'Done (' in line and not ready:
                ready = True
                stop_thread = threading.Thread(target=stop_server, daemon=True)
                stop_thread.start()
            if 'Encountered an unexpected exception' in line or 'Failed to start the minecraft server' in line: failure = True
    code = process.wait()
finally:
    timer.cancel()
if stop_thread is not None: stop_thread.join(timeout=20)
text = log.read_text()
if not ready or failure or code != 0 or stop_error or 'Stopping server' not in text or ('Saving chunks' not in text and 'Saving worlds' not in text):
    raise SystemExit(f'FAIL server startup/readiness/clean shutdown: ready={ready}, exception={failure}, exit={code}, stop={stop_error}')
print(f'PASS fresh headless NeoForge server reached readiness and saved/stopped cleanly in {time.monotonic()-started:.1f}s. No interactive gameplay was performed.')
