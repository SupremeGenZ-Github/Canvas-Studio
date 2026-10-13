"""Start a fresh headless server, require readiness, then request a clean save/stop."""
from pathlib import Path
import subprocess, threading, time, sys

root = Path(__file__).resolve().parents[1]
run = root / 'run'
run.mkdir(exist_ok=True)
(run / 'eula.txt').write_text('eula=true\n')
log = root / 'verification/server-smoke.log'
ready = False
failure = False
started = time.monotonic()
process = subprocess.Popen(['./gradlew', 'runServer', '--no-daemon', '--console=plain'], cwd=root, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, bufsize=1)

def deadline():
    if process.poll() is None:
        process.terminate()
        time.sleep(5)
        if process.poll() is None:
            process.kill()

timer = threading.Timer(300, deadline)
timer.start()
try:
    with log.open('w') as output:
        for line in process.stdout:
            output.write(line)
            output.flush()
            print(line, end='', flush=True)
            if 'Done (' in line and not ready:
                ready = True
                process.stdin.write('stop\n')
                process.stdin.flush()
            if 'Encountered an unexpected exception' in line or 'Failed to start the minecraft server' in line:
                failure = True
    code = process.wait()
finally:
    timer.cancel()
text = log.read_text()
if not ready or failure or code != 0 or ('Saving chunks' not in text and 'Saving worlds' not in text):
    raise SystemExit(f'FAIL server startup/readiness/clean shutdown: ready={ready}, exception={failure}, exit={code}')
print(f'PASS fresh headless NeoForge server reached readiness and saved/stopped cleanly in {time.monotonic()-started:.1f}s. No interactive gameplay was performed.')
