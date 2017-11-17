import math

def average(dataList):
    sum = 0.0
    for data in dataList:
        sum = sum + float(data)

    average = sum / len(dataList)
    return average

def standardDeviation(average, dataList):
    sum = 0.0
    for data in dataList:
        sum = sum + pow(float(data) - average, 2)

    standardDeviation = math.sqrt(sum / len(dataList))
    return standardDeviation

def standardizing(average, standardDeviation, dataList):
    standardizingList = []
    for data in dataList:
        z = (float(data) - average) / standardDeviation
        standardizingList.append(z)

    return standardizingList

def countFrequency(pieceCount, dataList):
    maxValue = 5.0
    minValue = -5.0
    totalLength = maxValue + abs(minValue)
    interval = totalLength / float(pieceCount)
    frequencyDistributionList = [0 for _ in range(pieceCount)]

    for data in dataList:
        if maxValue < float(data):
            frequencyDistributionList[pieceCount] = frequencyDistributionList[pieceCount] + 1
        else:
            for i in range(0, pieceCount):
                if float(data) < (minValue + i * interval):
                    frequencyDistributionList[i] = frequencyDistributionList[i] + 1
                    break

    return frequencyDistributionList

def probabilityDistribution(pieceCount, standardizingList):
    maxValue = 5.0
    minValue = -5.0
    totalLength = maxValue + abs(minValue)
    interval = totalLength / float(pieceCount)
    frequencyDistributionList = countFrequency(pieceCount, standardizingList)
    probabilityDistribution = [0 for _ in range(pieceCount)]

    for i in range(0, pieceCount):
        probabilityDistribution[i] = float(frequencyDistributionList[i]) / len(standardizingList)

    return probabilityDistribution