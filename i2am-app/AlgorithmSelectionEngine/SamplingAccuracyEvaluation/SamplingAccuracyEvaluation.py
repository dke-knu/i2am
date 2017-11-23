from . import SamplingAlgorithm as SA
from . import AccuracyEvaluation as AE

def populationListGenerate(filePath):
    populationList = []
    populationFile = open(filePath, 'r')
    lines = populationFile.readlines()
    for line in lines:
        populationList.append(line)
    populationFile.close()

    return populationList

def run(windowSize, sampleSize, jSDPieceCount, pAAPieceCount):

    count = 1
    windowList = []
    populationList =  populationListGenerate()

    for data in populationList:
        windowList.append(data)
        if count == windowSize:
            print('SystematicSampling')
            sampleList = SA.systematic(windowSize / sampleSize, windowList)
            AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)

            print('ReservoirSampling')
            sampleList = SA.sortedReservoir(sampleSize, windowList)
            AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)

            print('HashSampling')
            sampleList = SA.hash(sampleSize, windowList)
            AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)

            print('PrioritySampling')
            sampleList = SA.sortedPriority(sampleSize, windowList)
            AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)

            count = 0
            windowList = []

        count = count + 1