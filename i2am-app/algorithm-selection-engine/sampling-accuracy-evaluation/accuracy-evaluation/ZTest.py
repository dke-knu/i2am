import math

def startZTest(populationAverage, sampleAverage, populationStrdDeviation, sampleSize):
    standardError = populationStrdDeviation / math.sqrt(sampleSize)
    observedValue = (sampleAverage - populationAverage) / standardError

    print("ZTest: " + str(observedValue))