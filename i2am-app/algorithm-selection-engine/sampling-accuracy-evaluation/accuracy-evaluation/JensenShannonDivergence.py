import math

def startJSD(p, q):
    middle = [0 for _ in range(len(p))]
    for i in range(0, len(p)):
        middle[i] = (p[i] + q[i]) / 2
    divergence = (startKLD(p, middle) + startKLD(q, middle)) / 2

    print("JSD: " + str(divergence))

def startKLD(p, q):
    divergence = 0.0
    for i in range(0, len(p)):
        tmp = 0.0
        if p[i] != 0.0:
            tmp = p[i] * (math.log10(p[i]) - math.log10(q[i]))
        divergence = divergence + tmp

    return divergence