import numpy as np
from PeriodicClassification import ModelConfig as myConfig
"""
    Parameters
        path: user's sample data(1024, n)
        column_idx: program's target
        final's shape: (1, 1024) --> (#data, input_size)   
"""
def _reader(path, column_idx=0):
    list = []
    with open(path, 'r') as ifp:
        for i, line in enumerate(ifp):
            item = line.split(',')[column_idx]
            list.append(float(item))
        final = [list]

    return final

"""
    Parameters
        dataset: user's data list
"""
def _resize(dataset):
    results = []
    input_length = myConfig.INPUT_SIZE
    periodic_flag = False # need not
    for time_series in dataset:
        # split X and Y
        x_data = time_series
        x_len = len(x_data)
        if x_len == input_length:
            # print('Fit')
            results.append(time_series)
        elif x_len > input_length:  # PAA
            x_data = _PAA(x_data, input_length) # piecewise aggregate approximation
            result = x_data
            results.append(result)
        elif (x_len < input_length) & (periodic_flag):  # smaller than 1024 and have period
            # concatenate
            iter_val = int(input_length / x_len)
            mod_size = input_length % x_len
            resized_time_series = x_data * iter_val + x_data[:mod_size]
            results.append(resized_time_series)
        elif (x_len < input_length) & (periodic_flag):
            # inverse PAA
            x_data = _InversePAA(x_data, input_length)
            result = x_data
            results.append(result)

    return np.array(results)


def _PAA(target_list, input_length):
    resized_time_series = []
    target_len = len(target_list)
    width = int(target_len / input_length)
    rest = target_len % input_length
    for i in range(input_length - 1):  # reshaped length -1
        item_list = target_list[i * width:(i + 1) * width]  # sum list
        item = sum(item_list) / float(width)
        resized_time_series.append(item)
    if rest == 0:
        final_list = target_list[-width:]
        final_item = sum(final_list) / width
        resized_time_series.append(final_item)
    else:
        final_list = target_list[-rest:]
        final_item = sum(final_list) / rest
        resized_time_series.append(final_item)
    return resized_time_series


def _InversePAA(target_list, input_length):
    # inverse PAA
    resized_time_series = []
    target_len = len(target_list)
    # print('False')
    base_width = int(input_length / target_len)
    rest = input_length % target_len
    for i in range(target_len):
        # print('input - rest: ', x_len - rest)
        # print('base width: ', base_width)
        if i < (target_len - rest):
            # print('base: ',x_data[i])
            resized_time_series += [target_list[i]] * base_width
            # print(resized_time_series)
        else:
            # print('base + 1: ',x_data[i])
            resized_time_series += [target_list[i]] * (base_width + 1)
    return resized_time_series
