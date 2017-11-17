import socket
import threading

HOST = '114.70.235.43'
PORT = 7979
BUFFERSIZE = 1024
ADDRESS = (HOST, PORT)

serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print("Binding")
serverSocket.bind(ADDRESS)
print("Listening")
serverSocket.listen(1)

def samplingAlgorithmSelect(clientSocket, address):
    print("Start Sampling Algorithm Selection Engine for", address)
    data = clientSocket.recv()




while True:
    clientSocket, address = serverSocket.accept()
    print("Connected by", address)
    thread = threading.Thread(target=samplingAlgorithmSelect, args=(clientSocket, address))
    thread.start()




