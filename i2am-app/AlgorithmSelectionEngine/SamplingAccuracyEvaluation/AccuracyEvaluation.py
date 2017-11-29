from SamplingAccuracyEvaluation import AccuracyMeasure as AM
from SamplingAccuracyEvaluation import StatisticalCalculation as SC

def run(populationList, sampleList, jSDPieceCount, pAAPieceCount):
    evaluationList = []
    populationAverage = SC.average(populationList)
    sampleAverage = SC.average(sampleList)
    populationStrdDeviation = SC.standardDeviation(populationAverage, populationList)
    sampleStrdDeviation = SC.standardDeviation(sampleAverage, sampleList)
    standardizingPopulationList = SC.standardizing(populationAverage, populationStrdDeviation, populationList)
    standardizingSampleList = SC.standardizing(sampleAverage, sampleStrdDeviation, sampleList)

    print('Population Average: ', populationAverage)
    print('populationStrdDeviation: ', populationStrdDeviation)
    print('Sample Average: ', sampleAverage)
    print('Sample Standard Deviation: ', sampleStrdDeviation)
    print()

    # ED Parameters
    paaPopulationList = AM.startPAA(pAAPieceCount, standardizingPopulationList)
    paaSampleList = AM.startPAA(pAAPieceCount, standardizingSampleList)

    # JSD Parameters
    populationProbabilityDistribution = SC.probabilityDistribution(jSDPieceCount, standardizingPopulationList)
    sampleProbabilityDistribution = SC.probabilityDistribution(jSDPieceCount, standardizingSampleList)

    # evaluationList --> [0] = ZTEST, [1] = JSD, [2] = ED
    evaluationList.append(AM.startZTest(populationAverage, sampleAverage, populationStrdDeviation, len(sampleList)))
    evaluationList.append(AM.startJSD(populationProbabilityDistribution, sampleProbabilityDistribution))
    evaluationList.append(AM.startED(paaPopulationList, paaSampleList, pAAPieceCount))

    return evaluationList