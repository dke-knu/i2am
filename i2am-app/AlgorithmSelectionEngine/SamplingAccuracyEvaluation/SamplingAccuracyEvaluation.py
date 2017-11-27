from SamplingAccuracyEvaluation import SamplingAlgorithm as SA
from SamplingAccuracyEvaluation import AccuracyEvaluation as AE
from SamplingAccuracyEvaluation import PrintGraph as PG
from SamplingAccuracyEvaluation import StatisticalCalculation as SC
import operator

def populationListGenerate(filePath):
    populationList = []
    populationFile = open(filePath, 'r')
    lines = populationFile.readlines()
    for line in lines:
        populationList.append(line)
    populationFile.close()

    return populationList

def calculateScore(evalList):
    score = 0
    for i in range(len(evalList)):
        if i == 0:
            score = score + abs(evalList[i])/4
        else:
            score = score + abs(evalList[i])/3

    return score

def run(windowSize, sampleSize, filePath):
    count = 1
    numOfTrials = 1
    jSDPieceCount = 20
    pAAPieceCount = 20

    populationList =  populationListGenerate(filePath)
    windowList = []

    accuracyMeasureCount = 3
    evalDic = {}
    reservoirEvalList = [0.0 for _ in range(accuracyMeasureCount)]
    hashEvalList = [0.0 for _ in range(accuracyMeasureCount)]
    priorityEvalList = [0.0 for _ in range(accuracyMeasureCount)]

    for data in populationList:
        windowList.append(data)

        if count == windowSize:
            PG.printSimplePlot(windowList)

            print(str(numOfTrials)+'_ReservoirSampling')
            sampleList = SA.sortedReservoirSam(sampleSize, windowList)
            tempEvalList = AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)
            SC.sumPerIndex(reservoirEvalList, tempEvalList)

            print(str(numOfTrials)+'_HashSampling')
            sampleList = SA.hashSam(sampleSize, windowList)
            tempEvalList = AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)
            SC.sumPerIndex(hashEvalList, tempEvalList)

            print(str(numOfTrials)+'_PrioritySampling')
            sampleList = SA.sortedPrioritySam(sampleSize, windowList)
            tempEvalList = AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)
            SC.sumPerIndex(priorityEvalList, tempEvalList)

            numOfTrials = numOfTrials + 1
            count = 0
            windowList = []

        count = count + 1

    for i in range(accuracyMeasureCount):
        reservoirEvalList[i] = reservoirEvalList[i] / numOfTrials
        hashEvalList[i] = hashEvalList[i] / numOfTrials
        priorityEvalList[i] = priorityEvalList[i] / numOfTrials

    evalDic['reservoir'] = calculateScore(reservoirEvalList)
    evalDic['hash'] = calculateScore(hashEvalList)
    evalDic['priority'] = calculateScore(priorityEvalList)

    sortedEvalList = sorted(evalDic.items(), key = operator.itemgetter(1))

    return sortedEvalList[0][0]