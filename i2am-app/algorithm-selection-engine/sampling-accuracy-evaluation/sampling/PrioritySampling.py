import random
import math

def run(sampleSize, populationList):
    count = 1
    minimumIndex = 0
    minimumPriority = 1.0;
    sampleList = []
    priorityList = []
    dataDic = {}

    for data in populationList:
        if data in dataDic:
            weight = dataDic.get(data)
            dataDic[data] = weight + 1
        else:
            dataDic[data] = 1

        priority = math.pow(random.random, 1.0 / dataDic[data])

        if count <= sampleSize:
            sampleList.append(data)
            priorityList.append(data)
            if priority < minimumPriority:
                minimumIndex = count -1
                minimumPriority = priority
        else:
             if minimumPriority < priority:
                 sampleList[minimumIndex] = data
                 priorityList[minimumIndex] = priority
                 minimumPriority = 1.0
                 for i in range(0, len(sampleList)):
                     if priority[i] < minimumPriority:
                         minimumIndex = i
                         minimumPriority = priority
        count = count + 1

    return sampleList

def runSortedPS(sampleSize, populationList):
    count = 1
    minimumIndex = 0
    minimumPriority = 1.0;
    sampleList = []
    priorityList = []
    dataDic = {}

    for data in populationList:
        if data in dataDic:
            weight = dataDic.get(data)
            dataDic[data] = weight + 1
        else:
            dataDic[data] = 1

        priority = math.pow(random.random, 1.0 / dataDic[data])

        if count <= sampleSize:
            sampleList.append(data)
            priorityList.append(data)
            if priority < minimumPriority:
                minimumIndex = count - 1
                minimumPriority = priority
        else:
            if minimumPriority < priority:
                sampleList.pop(minimumIndex)
                priorityList.pop(minimumIndex)
                sampleList.append(data)
                priorityList.append(priority)
                minimumPriority = 1.0
                for i in range(0, len(sampleList)):
                    if priority[i] < minimumPriority:
                        minimumIndex = i
                        minimumPriority = priority
        count = count + 1

    return sampleList