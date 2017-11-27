import socket
import threading
import pymysql
from SamplingAccuracyEvaluation import SamplingAccuracyEvaluation as SAE
from PeriodicClassification import DeepLearning as DL

HOST = '114.70.235.43'
PORT = 7979
BUFFERSIZE = 1024
ADDRESS = (HOST, PORT)

serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM) # Create Socket
print("Binding")
serverSocket.bind(ADDRESS) # Binding
print("Listening")
serverSocket.listen(1) #Listening

def samplingAlgorithmSelect(clientSocket, address):
    print("Start Sampling Algorithm Selection Engine for", address)

    data = clientSocket.recv()
    name = ''
    userID = ''
    filePath = getFilePath(name, userID)
    flag = DL._DNN_main(filePath)

    if flag:
        print('systematic')
    else:
        selectionAlgorithm = SAE.run(1000, 200, filePath)
        print(selectionAlgorithm)

def getFilePath(name, userID):
    db = pymysql.connect(host = '114.70.235.43', port = 3306, user = 'plan-manager', passwd = 'dke214', db = 'i2am', charset = 'utf8', autocommit = True) # Connect MariaDB
    cursor = db.cursor()
    getFilePathSQL = "SELECT FILE_PATH FROM tbl_src_test_data WHERE NAME = %s AND F_OWNER = (SELECT IDX FROM tbl_user WHERE ID = %s)"
    cursor.execute(getFilePathSQL, (name, userID))
    filePath = cursor.fetchone()
    print(filePath)
    cursor.close()
    return filePath

selectedAlgorithm = SAE.run(1000, 200, 'D:\\pycharm-workspace\\실험데이터\\apple.csv')
print(selectedAlgorithm)

while True:
    clientSocket, address = serverSocket.accept() # Connect
    print("Connected by", address)
    thread = threading.Thread(target=samplingAlgorithmSelect, args=(clientSocket, address)) # Regist Thread
    thread.start()


