import math

def sumPerIndex(p, q):
    for i in range(len(p)):
        p[i] = p[i] + q[i]

    return p

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
    frequencyDistributionList = [0 for _ in range(pieceCount+2)] # 양쪽 최소, 최대값

    for data in dataList:
        if maxValue < float(data): # 왼쪽 끝 최소값
            frequencyDistributionList[pieceCount+1] = frequencyDistributionList[pieceCount+1] + 1
        elif float(data) < minValue: # 오른쪽 끝 최대값
            frequencyDistributionList[0] = frequencyDistributionList[0] + 1
        else:
            for i in range(1, pieceCount):
                if float(data) < (minValue + (i * interval)):
                    frequencyDistributionList[i] = frequencyDistributionList[i] + 1
                    break

    return frequencyDistributionList

def probabilityDistribution(pieceCount, standardizingList):
    frequencyDistributionList = countFrequency(pieceCount, standardizingList)
    probabilityDistribution = [0 for _ in range(pieceCount+2)]

    for i in range(0, pieceCount+2):
        probabilityDistribution[i] = float(frequencyDistributionList[i]) / float(len(standardizingList))

    return probabilityDistribution