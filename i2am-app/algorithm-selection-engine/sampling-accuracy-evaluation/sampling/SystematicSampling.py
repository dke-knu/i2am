import random

def run(interval, populationList):
    sampleList = []
    selectionNumber = random.randrange(0, interval)
    count = 0

    for data in populationList:
        if (count % interval) == selectionNumber:
            sampleList.append(data)
        count = count + 1

    return sampleList