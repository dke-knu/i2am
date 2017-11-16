import random

def run(sampleSize, populationList):
    bucketSize = int(len(populationList) / sampleSize)
    selectionBucket = random.randrange(0, bucketSize)
    sampleList = []

    for data in populationList:
        hashCode = hash(data) % bucketSize
        if hashCode == selectionBucket:
            sampleList.append(data)

    return sampleList