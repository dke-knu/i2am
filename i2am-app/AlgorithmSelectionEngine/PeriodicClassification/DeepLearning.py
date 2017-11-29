""" Learned classification model """
import tensorflow as tf
import time
from PeriodicClassification import ModelConfig as myConfig
from PeriodicClassification import Preprocess as pre


def _model(X, keep_prob):
    # input
    W1 = tf.Variable(tf.random_normal([myConfig.INPUT_SIZE, myConfig.HIDDEN_SIZE]), name="weight1")
    b1 = tf.Variable(tf.random_normal([myConfig.HIDDEN_SIZE]))
    L1 = tf.matmul(X, W1) + b1
    L1 = tf.nn.dropout(L1, keep_prob[0])

    """hidden Layers
        dropout: 
    """
    W2 = tf.Variable(tf.random_normal([myConfig.HIDDEN_SIZE, myConfig.HIDDEN_SIZE]), name="weight2")
    b2 = tf.Variable(tf.random_normal([myConfig.HIDDEN_SIZE]))
    L2 = tf.nn.softsign(tf.matmul(L1, W2) + b2)
    L2 = tf.nn.dropout(L2, keep_prob[1])

    W3 = tf.Variable(tf.random_normal([myConfig.HIDDEN_SIZE, myConfig.HIDDEN_SIZE]), name="weight3")
    b3 = tf.Variable(tf.random_normal([myConfig.HIDDEN_SIZE]))
    L3 = tf.nn.softsign(tf.matmul(L2, W3) + b3)
    L3 = tf.nn.dropout(L3, keep_prob[1])

    W4 = tf.Variable(tf.random_normal([myConfig.HIDDEN_SIZE, myConfig.HIDDEN_SIZE]), name="weight4")
    b4 = tf.Variable(tf.random_normal([myConfig.HIDDEN_SIZE]))
    L4 = tf.nn.softsign(tf.matmul(L3, W4) + b4)
    L4 = tf.nn.dropout(L4, keep_prob[1])

    W5 = tf.Variable(tf.random_normal([myConfig.HIDDEN_SIZE, myConfig.OUTPUT_SIZE]), name="weight5")
    b5 = tf.Variable(tf.random_normal([myConfig.OUTPUT_SIZE]))
    L5 = tf.nn.softsign(tf.matmul(L4, W5) + b5)

    hypothesis = tf.nn.dropout(L5, keep_prob[2])

    # weight paramenters and bias
    param_list = [W1, W2, W3, W4, W5, b1, b2, b3, b4, b5]

    saver = tf.train.Saver(param_list)

    return hypothesis, saver


def _classification(hypothesis):
    p = tf.nn.softmax(hypothesis)
    h_predict = tf.argmax(p, 1)

    return h_predict


def _DNN_main(USER_DATA_PATH):
    _start_msg()
    list_time_series = pre._reader(USER_DATA_PATH)
    time_series = pre._resize(list_time_series)

    X = tf.placeholder(tf.float32, [None, myConfig.INPUT_SIZE])
    keep_prob = tf.placeholder(tf.float32)  #0.1, 0.2, 0.3

    hypo, model_saver = _model(X=X, keep_prob=keep_prob)
    h_predict = _classification(hypothesis=hypo)

    """Initialize"""
    sess = tf.Session()
    sess.run(tf.global_variables_initializer())
    saver = tf.train.import_meta_graph(myConfig.SAVED_MODEL_PATH)
    saver.restore(sess, tf.train.latest_checkpoint(myConfig.CHECKPOINT_PATH))

    _load_model_msg(1)

    t_trained = sess.run([h_predict], feed_dict={X: time_series, keep_prob: [1.0, 1.0, 1.0]})
    print(t_trained[0])

    if t_trained[0] == 1:
        print('Non periodic')
        return False
    else:
        print('Periodic')
        return True

# Usage Example
# _DNN_main("user's data path")
# _DNN_main("D:/DKE/data/period_classification/시연데이터/ECG_데이터_1.csv")

def _start_msg():
    print("")
    print("***********************************************************")
    print("Start Classification with Deep Learning")
    print("                                 Model name: FFNN 1024")
    print("                                 Optimize func.: Adam")
    print("                                 Activation func.: softsign")
    print("                                 Learning rate: 0.0001")
    print("***********************************************************")
    print("")


def _load_model_msg(sleep_time):
    print("")
    time.sleep(sleep_time)
    print(" The classification model was successfully imported!")
