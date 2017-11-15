def startPAA(pieceCount, dataList):
    count = 0
    sum = 0.0
    i = 0
    interval = len(dataList) / pieceCount
    paaList = [0 for _ in range(pieceCount)]

    for data in dataList:
        sum = sum + float(data)
        if count == interval:
            average = sum / interval
            paaList[i] = average
            i = i + 1
            sum = 0.0
            count = 0
        count = count + 1

    return paaList