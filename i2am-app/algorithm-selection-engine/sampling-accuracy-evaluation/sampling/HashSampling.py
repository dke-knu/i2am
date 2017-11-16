import random
import math

def run(sampleSize, populationList):
    bucketSize = int(len(populationList) / sampleSize)
    selectionBucket = random.randrange(0, bucketSize)
    sampleList = []

    for data in populationList:
        hashCode = divmod(jSHash(data), bucketSize)
        if hashCode == selectionBucket:
            sampleList.append(data)

    return sampleList

def jSHash(data):
    hashCode = 1315423911
    for i in range(0, len(data)):
        hashCode = hashCode ^ ((hashCode << 5) + list(data)[i] + (hashCode >> 2))

    hashCode = abs(hashCode)
    return hashCode