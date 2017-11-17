from . import AccuracyMeasure as AM
from . import StatisticalCalculation as SC

def run(populationList, sampleList, jSDPieceCount, pAAPieceCount):
    populationAverage = SC.average(populationList)
    sampleAverage = SC.average(sampleList)
    populationStrdDeviation = SC.standardDeviation(populationAverage, populationList)
    sampleStrdDeviation = SC.standardDeviation(sampleAverage, sampleList)
    standardizingPopulationList = SC.standardizing(populationAverage, populationStrdDeviation, populationList)
    standardizingSampleList = SC.standardizing(sampleAverage, sampleStrdDeviation, sampleList)

    # ED Parameters
    paaPopulationList = AM.startPAA(pAAPieceCount, standardizingPopulationList)
    paaSampleList = AM.startPAA(pAAPieceCount, standardizingSampleList)

    # JSD Parameters
    populationProbabilityDistribution = SC.probabilityDistribution(jSDPieceCount, standardizingPopulationList)
    sampleProbabilityDistribution = SC.probabilityDistribution(jSDPieceCount, standardizingSampleList)

    AM.startZTest(populationAverage, sampleAverage, populationStrdDeviation, len(sampleList))
    AM.startJSD(populationProbabilityDistribution, sampleProbabilityDistribution)
    AM.startED(paaPopulationList, paaSampleList, pAAPieceCount)