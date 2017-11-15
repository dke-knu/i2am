from . import ZTest
from . import EuclideanDistance as ED
from . import StatisticalCalculation as SC
from . import JensenShannonDivergence  as JSD
from . import PiecewiseAggregateApproximation as PAA

def run(populationList, sampleList, jSDPieceCount, pAAPieceCount):
    populationAverage = SC.average(populationList)
    sampleAverage = SC.average(sampleList)
    populationStrdDeviation = SC.standardDeviation(populationAverage, populationList)
    sampleStrdDeviation = SC.standardDeviation(sampleAverage, sampleList)
    standardizingPopulationList = SC.standardizing(populationAverage, populationStrdDeviation, populationList)
    standardizingSampleList = SC.standardizing(sampleAverage, sampleStrdDeviation, sampleList)

    # ED Parameters
    paaPopulationList = PAA.startPAA(pAAPieceCount, standardizingPopulationList)
    paaSampleList = PAA.startPAA(pAAPieceCount, standardizingSampleList)

    # JSD Parameters
    populationProbabilityDistribution = SC.probabilityDistribution(jSDPieceCount, standardizingPopulationList)
    sampleProbabilityDistribution = SC.probabilityDistribution(jSDPieceCount, standardizingSampleList)

    ZTest.startZTest(populationAverage, sampleAverage, populationStrdDeviation, len(sampleList))
    JSD.startJSD(populationProbabilityDistribution, sampleProbabilityDistribution)
    ED.startED(paaPopulationList, paaSampleList, pAAPieceCount)