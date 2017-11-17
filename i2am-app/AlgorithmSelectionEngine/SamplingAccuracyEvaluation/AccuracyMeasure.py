import math

# ZTest
def startZTest(populationAverage, sampleAverage, populationStrdDeviation, sampleSize):
    standardError = populationStrdDeviation / math.sqrt(sampleSize)
    observedValue = (sampleAverage - populationAverage) / standardError

    print("ZTest: " + str(observedValue))

# Euclidean Distance
def startED(p, q, length):
    sum = 0.0
    for i in range(0, length):
        sum = math.pow(p[i] - q[i], 2) + sum

    euclideanDistance = math.sqrt(sum)

    print("ED: " + str(euclideanDistance))

# Piecewise Aggregate Approximation
def startPAA(pieceCount, dataList):
    count = 0
    sum = 0.0
    i = 0
    interval = len(dataList) / pieceCount
    paaList = [0 for _ in range(pieceCount)]

    for data in dataList:
        sum = sum + float(data)
        if count == interval:
            average = sum / interval
            paaList[i] = average
            i = i + 1
            sum = 0.0
            count = 0
        count = count + 1

    return paaList

# Jensen Shannon Divergence
def startJSD(p, q):
    middle = [0 for _ in range(len(p))]
    for i in range(0, len(p)):
        middle[i] = (p[i] + q[i]) / 2
    divergence = (startKLD(p, middle) + startKLD(q, middle)) / 2

    print("JSD: " + str(divergence))

# Kullback Leibler Divergence
def startKLD(p, q):
    divergence = 0.0
    for i in range(0, len(p)):
        tmp = 0.0
        if p[i] != 0.0:
            tmp = p[i] * (math.log10(p[i]) - math.log10(q[i]))
        divergence = divergence + tmp

    return divergence

