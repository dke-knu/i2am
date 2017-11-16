import random

def run(sampleSize, populationList):
    sampleList = []
    count = 1
    for data in populationList:
        if count <= sampleSize:
            sampleList.append(data)
        else:
            probability = random.randrange(0, count)
            if probability < sampleSize:
                sampleList[count] = data
        count = count + 1

    return sampleList

def runSortedRS(sampleSize, populationList):
    sampleList = []
    count = 1
    for data in populationList:
        if count <= sampleSize:
            sampleList.append(data)
        else:
            probability = random.randrange(0, count)
            if probability < sampleSize:
                sampleList.pop(count)
                sampleList.append(data)
        count = count + 1

    return sampleList