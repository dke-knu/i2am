import socket
import threading
import pymysql
import json
from SamplingAccuracyEvaluation import SamplingAccuracyEvaluation as SAE
from PeriodicClassification import CNN as DL
from kafka import KafkaConsumer as KC
from collections import OrderedDict

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
    jsonData = json.load(data)
    if jsonData['message'] == 'new-source':
        userID = jsonData['user-id']
        sourceName = jsonData['src-name']
        print("User ID: " + str(userID))
        print("Source Name: " + str(sourceName))
        filePath = getFilePath(sourceName, userID)

    elif jsonData['message'] == 'concept-drift':
        sourceName = jsonData['src-name']
        userID = jsonData['user-id']
        partition = jsonData['partition']
        offset = jsonData['offset']
        topic = getTopicName(sourceName, userID)
        filePath = getStreamDataFromKafka(topic, partition, offset)

    flag = DL._CNN_main(filePath)

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

    if jsonData['message'] == 'new-source':
        putSelectedAlgorithm(sourceName, userID, selectedAlgorithm)
    elif jsonData['message'] == 'concept-drift':
        sendSelectedAlgorithm(sourceName, userID, selectedAlgorithm)

def connectToDB():
    print("Connect To MariaDB")
    db = pymysql.connect(host='114.70.235.43', port=3306, user='plan-manager', passwd='dke214', db='i2am', charset='utf8', autocommit=True)  # Connect MariaDB
    cursor = db.cursor()
    print("Connected Succeeded")
    return cursor

def getFilePath(sourceName, userID):
    print("Get File Path from DB")
    cursor = connectToDB()
    getFilePathSQL = "SELECT FILE_PATH FROM tbl_src_test_data WHERE IDX = ( SELECT F_TEST_DATA FROM tbl_src WHERE NAME = %s AND F_OWNER = ( SELECT IDX FROM tbl_user WHERE ID = %s))"
    cursor.execute(getFilePathSQL, (sourceName, userID))
    filePath = cursor.fetchone()
    print("File Path: " + str(filePath).split("'")[1])
    cursor.close()
    return str(filePath).split("'")[1]

def getTopicName(sourceName, userID):
    print("Get Topic Name from DB")
    cursor = connectToDB()
    getTopicNameSQL = "SELECT TOPIC_NAME FROM topic_list WHERE (NAME = %s) AND F_OWNER = (SELECT IDX FROM tbl_user WHERE ID = %s)"
    cursor.execute(getTopicNameSQL, (sourceName, userID))
    topicName = cursor.fetchone()
    print("Topic Name: ", topicName)
    cursor.close()
    return topicName

def putSelectedAlgorithm(sourceName, userID, selectedAlgorithm):
    print("Put Selected Algorithm")
    db = pymysql.connect(host='114.70.235.43', port=3306, user='plan-manager', passwd='dke214', db='i2am', charset='utf8', autocommit=True)  # Connect MariaDB
    cursor = db.cursor()
    putSelectedAlgorithmSQL = "UPDATE tbl_src SET RECOMMENDED_SAMPLING = %s WHERE NAME = %s AND F_OWNER = (SELECT IDX FROM tbl_user WHERE ID = %s)"
    cursor.execute(putSelectedAlgorithmSQL, (selectedAlgorithm, sourceName, userID))
    print("Put Recommended Algorithm to DB")
    cursor.close()

def getStreamDataFromKafka(topic, partition, offset):
    consumer = KC(topic, group_id='my-group', bootstrap_servers=['MN:9092'])
    consumer.seek(partition, offset)
    testDataArray = []
    for message in consumer:
        testDataArray.append(message.value)
        filePath = '/data/', topic, '_', offset, '.csv'
        if len(testDataArray) == 1024:
            file = open(filePath, 'w')
            for data in testDataArray:
                file.write(data, '\n')
            break

    return filePath

def sendSelectedAlgorithm(sourceName, userID, selectedAlgorithm):
    HOST = 'MN'
    PORT = '1234'
    ADDRESS = (HOST, PORT)
    clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    try:
        clientSocket.connect(ADDRESS)
        print("Connected by", ADDRESS)
    except Exception as e:
        print("Connection Failed ", ADDRESS)
        return False

    print("Connection Succeeded ", ADDRESS)
    message = OrderedDict()
    message['message'] = 'new-algorithm'
    message['src-name'] = sourceName
    message['user-id'] = userID
    message['recommendation'] = selectedAlgorithm
    jsonMessage = json.dump(message)

    try:
        clientSocket.send(jsonMessage)
        print("Send Json Message to ", ADDRESS)
    except Exception as e:
        print("Send Failed")
        return False

while True:
    clientSocket, address = serverSocket.accept() # Connect
    print("Connected by", address)
    thread = threading.Thread(target=samplingAlgorithmSelect, args=(clientSocket, address)) # Regist Thread
    thread.start()