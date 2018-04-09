import math

# ZTest
def startZTest(populationAverage, sampleAverage, populationStrdDeviation, sampleSize):
    standardError = populationStrdDeviation / math.sqrt(sampleSize)
    observedValue = (sampleAverage - populationAverage) / standardError

    print("ZTest: " + str(observedValue))
    return observedValue

# Euclidean Distance
def startED(p, q, length):
    sum = 0.0
    for i in range(0, length):
        sum = math.pow(p[i] - q[i], 2) + sum

    euclideanDistance = math.sqrt(sum)
    print("ED: " + str(euclideanDistance))
    return euclideanDistance

# Piecewise Aggregate Approximation
def startPAA(pieceCount, dataList):
    count = 0
    remainderCount = 1
    sum = 0.0
    i = 0
    interval = int(len(dataList) / pieceCount)
    remainder = len(dataList) % pieceCount
    paaList = [0 for _ in range(pieceCount)]

    for data in dataList:
        sum = sum + float(data)
        count = count + 1
        if remainderCount <= remainder:
            if count == (interval + 1):
                average = sum / interval + 1
                paaList[i] = average
                remainderCount = remainderCount + 1
                i = i + 1
                sum = 0.0
                count = 0
        else:
            if count == interval:
                average = sum / interval
                paaList[i] = average
                i = i + 1
                sum = 0.0
                count = 0

    return paaList

# Jensen Shannon Divergence
def startJSD(p, q):
    middle = [0 for _ in range(len(p))]
    for i in range(0, len(p)):
        middle[i] = (p[i] + q[i]) / 2
    divergence = (startKLD(p, middle) + startKLD(q, middle)) / 2

    print("JSD: " + str(divergence))
    return divergence

# Kullback Leibler Divergence
def startKLD(p, q):
    divergence = 0.0
    for i in range(0, len(p)):
        tmp = 0.0
        if p[i] != 0.0:
            tmp = p[i] * (math.log10(p[i]) - math.log10(q[i]))
        divergence = divergence + tmp

    return divergence

