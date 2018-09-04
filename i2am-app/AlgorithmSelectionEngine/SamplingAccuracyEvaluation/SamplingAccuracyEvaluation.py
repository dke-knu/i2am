from SamplingAccuracyEvaluation import SamplingAlgorithm as SA
from SamplingAccuracyEvaluation import AccuracyEvaluation as AE
from SamplingAccuracyEvaluation import PrintGraph as PG
from SamplingAccuracyEvaluation import StatisticalCalculation as SC
import operator

def populationListGenerate(filePath, target):
    print('Generate Population List')
    populationList = []
    populationFile = open(filePath, 'r')

    while True:
        line = populationFile.readline()
        if not line: break
        line_data = line.split(',')
        populationList.append(line_data[target])

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

def run(windowSize, sampleSize, filePath, target=0):
    print('############## Sampling Accuracy Evaluation ##############')
    count = 1
    numOfTrials = 1
    jSDPieceCount = 20
    pAAPieceCount = 20
    print('Window Size: ' ,windowSize)
    print('Sample Size: ' ,sampleSize)
    print('JSD Piece Count: ' ,jSDPieceCount)
    print('PAA Piece Count: ' ,pAAPieceCount)
    populationList = populationListGenerate(filePath, target)
    windowList = []

    accuracyMeasureCount = 3
    evalDic = {}
    reservoirEvalList = [0.0 for _ in range(accuracyMeasureCount)]
    hashEvalList = [0.0 for _ in range(accuracyMeasureCount)]
    priorityEvalList = [0.0 for _ in range(accuracyMeasureCount)]
    print()

    for data in populationList:
        windowList.append(data)
        if count == windowSize:
            print('################## ' + str(numOfTrials) + ' Evaluation Start ####################')
            # if numOfTrials == 1: PG.printGraph(windowList, 'Population', numOfTrials)
            print()

            print(str(numOfTrials)+'_ReservoirSampling')
            sampleList = SA.sortedReservoirSam(sampleSize, windowList)
            tempEvalList = AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)
            SC.sumPerIndex(reservoirEvalList, tempEvalList)
            # if numOfTrials == 1: PG.printGraph(sampleList, 'Reservoir', numOfTrials)
            print()

            print(str(numOfTrials)+'_HashSampling')
            sampleList = SA.hashSam(sampleSize, windowList)
            tempEvalList = AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)
            SC.sumPerIndex(hashEvalList, tempEvalList)
            # if numOfTrials == 1: PG.printGraph(sampleList, 'Hash', numOfTrials)
            print()

            print(str(numOfTrials)+'_PrioritySampling')
            sampleList = SA.sortedPrioritySam(sampleSize, windowList)
            tempEvalList = AE.run(windowList, sampleList, jSDPieceCount, pAAPieceCount)
            SC.sumPerIndex(priorityEvalList, tempEvalList)
            # if numOfTrials == 1: PG.printGraph(sampleList, 'Priority', numOfTrials)
            print()

            numOfTrials = numOfTrials + 1
            count = 0
            windowList = []

        count = count + 1

    for i in range(accuracyMeasureCount):
        reservoirEvalList[i] = reservoirEvalList[i] / numOfTrials
        hashEvalList[i] = hashEvalList[i] / numOfTrials
        priorityEvalList[i] = priorityEvalList[i] / numOfTrials

    evalDic['RESERVOIR_SAMPLING'] = calculateScore(reservoirEvalList)
    evalDic['HASH_SAMPLING'] = calculateScore(hashEvalList)
    evalDic['PRIORITY_SAMPLING'] = calculateScore(priorityEvalList)

    sortedEvalList = sorted(evalDic.items(), key = operator.itemgetter(1))

    return sortedEvalList[0][0]