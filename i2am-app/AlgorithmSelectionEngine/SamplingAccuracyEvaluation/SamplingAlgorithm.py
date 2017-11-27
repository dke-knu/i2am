import random
import math

# Systematic Sampling
def systematicSam(interval, populationList):
    sampleList = []
    selectionNumber = random.randrange(0, interval)
    count = 0

    for data in populationList:
        if (count % interval) == selectionNumber:
            sampleList.append(data)
        count = count + 1

    return sampleList

# Reservoir Sampling
def reservoirSam(sampleSize, populationList):
    sampleList = []
    count = 1
    for data in populationList:
        if count <= sampleSize:
            sampleList.append(data)
        else:
            probability = random.randrange(0, count)
            if probability < sampleSize:
                sampleList[probability] = data
        count = count + 1

    return sampleList

def sortedReservoirSam(sampleSize, populationList):
    sampleList = []
    count = 1
    for data in populationList:
        if count <= sampleSize:
            sampleList.append(data)
        else:
            probability = random.randrange(0, count)
            if probability < sampleSize:
                sampleList.pop(probability)
                sampleList.append(data)
        count = count + 1

    return sampleList

# Hash Sampling
def hashSam(sampleSize, populationList):
    bucketSize = int(len(populationList) / sampleSize)
    selectionBucket = random.randrange(0, bucketSize)
    sampleList = []

    for data in populationList:
        hashCode = hash(data) % bucketSize
        if hashCode == selectionBucket:
            sampleList.append(data)

    return sampleList

# Priority Sampling
def prioritySam(sampleSize, populationList):
    count = 1
    minimumIndex = 0
    minimumPriority = 1.0
    sampleList = []
    priorityList = []
    dataDic = {}

    for data in populationList:
        if data in dataDic:
            weight = dataDic.get(data)
            dataDic[data] = weight + 1
        else:
            dataDic[data] = 1

        priority = math.pow(random.random(), 1.0 / float(dataDic[data]))

        if count <= sampleSize:
            sampleList.append(data)
            priorityList.append(priority)
            if priority < minimumPriority:
                minimumIndex = count -1
                minimumPriority = priority
        else:
             if minimumPriority < priority:
                 sampleList[minimumIndex] = data
                 priorityList[minimumIndex] = priority
                 minimumPriority = 1.0
                 for i in range(0, len(sampleList)):
                     if priorityList[i] < minimumPriority:
                         minimumIndex = i
                         minimumPriority = priorityList[i]
        count = count + 1

    return sampleList

def sortedPrioritySam(sampleSize, populationList):
    count = 1
    minimumIndex = 0
    minimumPriority = 1.0
    sampleList = []
    priorityList = []
    dataDic = {}

    for data in populationList:
        if data in dataDic:
            weight = dataDic.get(data)
            dataDic[data] = weight + 1
        else:
            dataDic[data] = 1

        priority = math.pow(random.random(), 1.0 / float(dataDic[data]))

        if count <= sampleSize:
            sampleList.append(data)
            priorityList.append(priority)
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
                for i in range(0, len(priorityList)):
                    if priorityList[i] < minimumPriority:
                        minimumIndex = i
                        minimumPriority = priorityList[i]
        count = count + 1

    return sampleList