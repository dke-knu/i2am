import socket
import threading
import pymysql
from SamplingAccuracyEvaluation import SamplingAccuracyEvaluation as SAE
from PeriodicClassfication import DeepLearning as DL

HOST = '114.70.235.43'
PORT = 7979
BUFFERSIZE = 1024
ADDRESS = (HOST, PORT)

# serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# print("Binding")
# serverSocket.bind(ADDRESS)
# print("Listening")
# serverSocket.listen(1)

def samplingAlgorithmSelect(clientSocket, address):
    print("Start Sampling Algorithm Selection Engine for", address)
    data = clientSocket.recv()

# Connect MariaDB
db = pymysql.connect(host = '114.70.235.43', port = 3306, user = 'plan-manager', passwd = 'dke214', db = 'i2am', charset = 'utf8', autocommit = True)
cursor = db.cursor()
getFilePathSQL = "SELECT FILE_PATH FROM tbl_src_test_data WHERE NAME = %s AND F_OWNER = (SELECT IDX FROM tbl_user WHERE ID = %s)"
cursor.execute(getFilePathSQL, ('abc', 'abc@naver.com'))
filePath = cursor.fetchone()
print(filePath)
cursor.close()

flag = DL._DNN_main('/home/admin/whcho/algorithm-selection-engine/Temperature1.txt')

if flag:
    print('systematic')
else:
    selectionAlgorithm = SAE.run(1000, 200, '/home/admin/whcho/algorithm-selection-engine/Temperature1.txt')
    print(selectionAlgorithm)

# while True:
#     clientSocket, address = serverSocket.accept()
#     print("Connected by", address)
#     thread = threading.Thread(target=samplingAlgorithmSelect, args=(clientSocket, address))
#     thread.start()


