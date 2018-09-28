import socket
import threading
import pymysql
import json
import sys
from SamplingAccuracyEvaluation import SamplingAccuracyEvaluation as SAE
from PeriodicClassification import CNN as DL
from kafka import KafkaConsumer as KC
from kafka import TopicPartition as TP
from collections import OrderedDict
from ASEConfig import DB_INFO, QUERY_DIC

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

HOST = ''
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
    jsonData = json.loads(data)
    userID = jsonData['user-id']
    sourceName = jsonData['src-name']

    if jsonData['message'] == 'new-src':
        print("User ID: " + str(userID))
        print("Source Name: " + str(sourceName))
        filePath = getFilePath(sourceName, userID)

    elif jsonData['message'] == 'concept-drift':
        partition = jsonData['partition']
        offset = jsonData['offset']
        print("src-name:", sourceName, "user-id:", userID, "partition:", partition, "offset:", offset)
        topic = getTopicName(sourceName, userID)
        filePath = getStreamDataFromKafka(topic, partition, offset)

    target_index, csv_schema_idx = get_target_index(user_id=userID, src_name=sourceName)

    flag = DL._CNN_main(filePath, target_index)

    if flag:
        selectedAlgorithm = SAE.run(1000, 200, filePath, target_index)
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

    putSelectedAlgorithm(sourceName, csv_schema_idx, selectedAlgorithm)
    logger(jsonData, selectedAlgorithm)

def connectToDB():
    print("Connect To MariaDB")
    db = pymysql.connect(host=DB_INFO['HOST'], port=DB_INFO['PORT'], user=DB_INFO['USER'], passwd=DB_INFO['PASSWD'], db=DB_INFO['DB'], charset='utf8', autocommit=True)  # Connect MariaDB
    cursor = db.cursor()
    print("Connected Succeeded")
    return cursor

def getFilePath(sourceName, userID):
    print("Get File Path from DB")
    cursor = connectToDB()
    cursor.execute(QUERY_DIC['GET_FILE_PATH'], (sourceName, userID))
    filePath = cursor.fetchone()
    print("File Path: " + str(filePath).split("'")[1])
    cursor.close()
    return str(filePath).split("'")[1]

def getTopicName(sourceName, userID):
    print("Get Topic Name from DB")
    cursor = connectToDB()
    cursor.execute(QUERY_DIC['GET_TOPIC_NAME'], (sourceName, userID))
    topicName = cursor.fetchone()
    print("Topic Name:", topicName[0])
    cursor.close()
    return topicName[0]

def putSelectedAlgorithm(sourceName, target, selectedAlgorithm):
    print("Put Selected Algorithm")
    cursor = connectToDB()
    cursor.execute(QUERY_DIC['PUT_SELECTED_ALGO'], (selectedAlgorithm, target, sourceName))
    print("Put Recommended Algorithm to DB")
    cursor.close()

def getStreamDataFromKafka(topic, partition, offset):
    consumer = KC(bootstrap_servers=['MN:9092'])
    print('topic:', topic, 'partition:', partition, 'offset', offset)
    topicPartition = TP(topic, partition)
    consumer.assign([topicPartition])
    consumer.seek(topicPartition, offset)
    testDataArray = []
    for message in consumer:
        testDataArray.append(message.value.decode('utf-8'))
        filePath = '/data/'+str(topic)+'_'+str(offset)+'.csv'
        if len(testDataArray) == 10240:
            file = open(filePath, 'w')
            for data in testDataArray:
                file.write(data+'\n')
            file.close()
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
    jsonMessage = json.dumps(message)

    try:
        clientSocket.send(jsonMessage)
        print("Send Json Message to ", ADDRESS)
    except Exception as e:
        print("Send Failed")
        return False

def get_target_index(user_id, src_name):
    cursor = connectToDB()
    try:
        cursor.execute(QUERY_DIC['GET_TARGET_IDX'], (src_name, user_id))
        target_info = cursor.fetchone()
    except Exception as e:
        target_info = (1, 1)
        print("Target not found. Initialized 1")

    return target_info[1], target_info[0]    # maria DB start idx from 0

def logger(json_data, selected_algorithm):
    cursor = connectToDB()
    try:
        if json_data['message'] == 'new-src':
            log_msg = '[INTELLIGENT ENGINE] {} is recommended.'.format(selected_algorithm)
        elif json_data['message'] == 'concept-drift':
            log_msg = '[INTELLIGENT ENGINE] The sampling algorithm has been changed to {}.'.format(selected_algorithm)

        cursor.execute(QUERY_DIC['WRITE_LOG'], (json_data['user-id'], 'INFO', log_msg))
    except Exception as e:
        print("Logging failed.")
        print(str(e))

while True:
    clientSocket, address = serverSocket.accept() # Connect
    print("Connected by", address)
    thread = threading.Thread(target=samplingAlgorithmSelect, args=(clientSocket, address)) # Regist Thread
    thread.start()

# Test Mode
print(' TEST MODE '.center(30, '#'))
userID = 'sbpark@kangwon.ac.kr'
sourceName = 'sb_test'

target_index, idx = get_target_index(user_id=userID, src_name=sourceName)
print("TARGET IDX: {} and CSV_IDX: {}".format(target_index, idx))

testWindowSize = sys.argv[1]
testSampleSize = sys.argv[2]
testFilePath = sys.argv[3]

DL._CNN_main(testFilePath, target_index)

SAE.run(testWindowSize, testSampleSize, testFilePath, target_index)