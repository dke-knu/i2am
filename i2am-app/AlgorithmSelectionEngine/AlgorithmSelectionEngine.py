import socket
import threading
import pymysql
from SamplingAccuracyEvaluation import SamplingAccuracyEvaluation as SAE
from PeriodicClassification import DeepLearning as DL

print(' _______  ___      _______  _______  ______    ___   _______  __   __  __   __')
print('|   _   ||   |    |       ||       ||    _ |  |   | |       ||  | |  ||  |_|  |')
print('|  |_|  ||   |    |    ___||   _   ||   | ||  |   | |_     _||  |_|  ||       |')
print('|       ||   |    |   | __ |  | |  ||   |_||_ |   |   |   |  |       ||       |')
print('|       ||   |___ |   ||  ||  |_|  ||    __  ||   |   |   |  |       ||       |')
print('|   _   ||       ||   |_| ||       ||   |  | ||   |   |   |  |   _   || ||_|| |')
print('|__| |__||_______||_______||_______||___|  |_||___|   |___|  |__| |__||_|   |_|')
print(' _______  _______  ___      _______  _______  _______  ___   _______  __    __ ')
print('|       ||       ||   |    |       ||       ||       ||   | |       ||  |  |  |')
print('|  _____||    ___||   |    |    ___||       ||_     _||   | |   _   ||   |_|  |')
print('| |_____ |   |___ |   |    |   |___ |       |  |   |  |   | |  | |  ||        |')
print('|_____  ||    ___||   |___ |    ___||      _|  |   |  |   | |  |_|  ||  _     |')
print(' _____| ||   |___ |       ||   |___ |     |_   |   |  |   | |       || | |    |')
print('|_______||_______||_______||_______||_______|  |___|  |___| |_______||_|  |___|')
print('              _______  __    _  _______  ___   __    _  _______                ')
print('             |       ||  |  | ||       ||   | |  |  | ||       |               ')
print('             |    ___||   |_| ||    ___||   | |   |_| ||    ___|               ')
print('             |   |___ |       ||   | __ |   | |       ||   |___                ')
print('             |    ___||  _    ||   ||  ||   | |  _    ||    ___|               ')
print('             |   |___ | | |   ||   |_| ||   | | | |   ||   |___                ')
print('             |_______||_|  |__||_______||___| |_|  |__||_______|               ')

HOST = 'MN'
PORT = 7979
BUFFERSIZE = 1024
ADDRESS = (HOST, PORT)

serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM) # Create Socket
print("Binding")
serverSocket.bind(ADDRESS) # Binding
print("Listening")
serverSocket.listen(1) #Listening

def samplingAlgorithmSelect(clientSocket, address):
    print("Start Sampling Algorithm Selection Engine for", address, "!!!")

    data = clientSocket.recv(BUFFERSIZE)
    print("Data Received")
    data = data.decode()
    userID = data.split(',')[0]
    name = data.split(',')[1]
    print("User ID: " + str(userID))
    print("Source Name: "+ str(name))
    filePath = getFilePath(name, userID)

    flag = DL._DNN_main(filePath)

    if flag:
        selectedAlgorithm = SAE.run(1000, 200, filePath)
        if selectedAlgorithm == 'RESERVOIR_SAMPLING':
            print('############### Recommendation Algorithm #################')
            print('#                                                        #')
            print('#                  RESERVOIR_SAMPLING                    #')
            print('#                                                        #')
            print('##########################################################')
        elif selectedAlgorithm == 'HASH_SAMPLING':
            print('############### Recommendation Algorithm #################')
            print('#                                                        #')
            print('#                     HASH_SAMPLING                      #')
            print('#                                                        #')
            print('##########################################################')
        elif selectedAlgorithm == 'PRIORITY_SAMPLIUNG':
            print('############### Recommendation Algorithm #################')
            print('#                                                        #')
            print('#                  PRIORITY_SAMPLING                     #')
            print('#                                                        #')
            print('##########################################################')
        print()
    else:
        selectedAlgorithm = 'SYSTEMATIC_SAMPLING'
        print('############### Recommendation Algorithm #################')
        print('#                                                        #')
        print('#                 SYSTEMATIC_SAMPLING                    #')
        print('#                                                        #')
        print('##########################################################')

    putSelectedAlgorithm(name, userID, selectedAlgorithm)

def getFilePath(name, userID):
    print("Get File Path from DB")
    db = pymysql.connect(host = '114.70.235.43', port = 3306, user = 'plan-manager', passwd = 'dke214', db = 'i2am', charset = 'utf8', autocommit = True) # Connect MariaDB
    cursor = db.cursor()
    print("DB Connected")
    getFilePathSQL = "SELECT FILE_PATH FROM tbl_src_test_data WHERE IDX = ( SELECT F_TEST_DATA FROM tbl_src WHERE NAME = %s AND F_OWNER = ( SELECT IDX FROM tbl_user WHERE ID = %s))"
    cursor.execute(getFilePathSQL, (name, userID))
    filePath = cursor.fetchone()
    print("File Path: " + str(filePath).split("'")[1])
    cursor.close()
    return str(filePath).split("'")[1]

def putSelectedAlgorithm(name, userID, selectedAlgorithm):
    print("Put Selected Algorithm")
    db = pymysql.connect(host='114.70.235.43', port=3306, user='plan-manager', passwd='dke214', db='i2am', charset='utf8', autocommit=True)  # Connect MariaDB
    cursor = db.cursor()
    putSelectedAlgorithmSQL = "UPDATE tbl_src SET RECOMMENDED_SAMPLING = %s WHERE NAME = %s AND F_OWNER = (SELECT IDX FROM tbl_user WHERE ID = %s )"
    cursor.execute(putSelectedAlgorithmSQL, (selectedAlgorithm, name, userID))
    print("Put Recommended Algorithm to DB")
    cursor.close()

while True:
    clientSocket, address = serverSocket.accept() # Connect
    print("Connected by", address)
    thread = threading.Thread(target=samplingAlgorithmSelect, args=(clientSocket, address)) # Regist Thread
    thread.start()