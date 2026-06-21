import socket
import json
import threading
import time

HOST = '127.0.0.1'
PORT = 8080

def handle_client(conn, addr):
    print(f"[SlabServer] Client connected from: {addr}")
    
    # Create thread to periodically send ping packets to the client
    stop_ping = threading.Event()
    def send_pings():
        while not stop_ping.is_set():
            time.sleep(5)
            if stop_ping.is_set():
                break
            try:
                ping_packet = json.dumps({"type": "ping"}) + "\n"
                conn.sendall(ping_packet.encode('utf-8'))
                print("[SlabServer] Sent: ping")
            except Exception as e:
                print(f"[SlabServer] Ping failed (client probably disconnected): {e}")
                break
                
    ping_thread = threading.Thread(target=send_pings)
    ping_thread.daemon = True
    ping_thread.start()

    try:
        # read line-delimited JSON packets
        rfile = conn.makefile('r', encoding='utf-8')
        for line in rfile:
            line = line.strip()
            if not line:
                continue
                
            try:
                data = json.loads(line)
                print(f"[SlabServer] Received: {data}")
                
                # Check for handshake hello
                if data.get("type") == "hello":
                    response = {
                        "type": "hello_response",
                        "status": "ok",
                        "server": "Python Test Slab Server",
                        "message": f"Hello {data.get('client', 'Player')}! Connection established."
                    }
                    conn.sendall((json.dumps(response) + "\n").encode('utf-8'))
                    print(f"[SlabServer] Responded: {response}")
                
                # Check for pong responses
                elif data.get("type") == "pong":
                    print("[SlabServer] Client responded with: pong (connection is active!)")
                    
            except json.JSONDecodeError:
                print(f"[SlabServer] Received non-JSON or malformed packet: {line}")
                
    except Exception as e:
        print(f"[SlabServer] Connection error with {addr}: {e}")
    finally:
        stop_ping.set()
        conn.close()
        print(f"[SlabServer] Client disconnected: {addr}")

def main():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # Allow address reuse to prevent "Address already in use" errors during quick restarts
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    
    try:
        server.bind((HOST, PORT))
        server.listen(5)
        print(f"[SlabServer] Listening on TCP {HOST}:{PORT}...")
        
        while True:
            conn, addr = server.accept()
            client_thread = threading.Thread(target=handle_client, args=(conn, addr))
            client_thread.daemon = True
            client_thread.start()
            
    except KeyboardInterrupt:
        print("\n[SlabServer] Shutting down.")
    finally:
        server.close()

if __name__ == '__main__':
    main()
