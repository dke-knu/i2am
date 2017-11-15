import math

def startED(p, q, length):
    sum = 0.0
    for i in range(0, length):
        sum = math.pow(p[i] - q[i], 2) + sum

    euclideanDistance = math.sqrt(sum)

    print("ED: " + str(euclideanDistance))