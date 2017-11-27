import math
import matplotlib.pyplot as plt

def printSimplePlot(dataList):
    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1)
    X = range(len(dataList))
    Y = [data for data in dataList]
    ax.get_yaxis().set_visible(False)
    ax.plot(X, Y)
    plt.show()

def printSinGraph():
    T = range(100)
    X = [(2 * math.pi * t) / len(T) for t in T]
    Y = [math.sin(value) for value in X]
    plt.plot(X, Y)
    plt.show()