""" Learned classification model """
import tensorflow as tf
import time
from PeriodicClassification import ModelConfig as myConfig
from PeriodicClassification import Preprocess as pre


def _model(X):

    # width( size of feature map), input channel, output channel
    W1 = tf.Variable(tf.random_normal([8, 1, 3], stddev=0.01))
    C1 = tf.nn.conv1d(value=X, filters=W1, stride=1, padding='SAME', name='conv1')
    C1 = tf.nn.tanh(C1)

    W2 = tf.Variable(tf.random_normal([16, 3, 5]))
    C2 = tf.nn.conv1d(value=C1, filters=W2, stride=1, padding='SAME', name='conv2')
    C2 = tf.nn.tanh(C2)

    W3 = tf.Variable(tf.random_normal([8, 5, 3]))
    C3 = tf.nn.conv1d(value=C2, filters=W3, stride=1, padding='SAME', name='conv3')
    C3 = tf.nn.tanh(C3)

    P1 = tf.layers.max_pooling1d(inputs=C3, pool_size=2, padding='SAME', strides=2)

    flat = tf.reshape(P1, [-1, 512*3])

    W4 = tf.Variable(tf.random_normal([512*3, 200]))
    b4 = tf.Variable(tf.random_normal([200]))
    dense4 = tf.nn.softsign(tf.matmul(flat, W4) + b4)

    W5 = tf.Variable(tf.random_normal([200, 2]))
    b5 = tf.Variable(tf.random_normal([2]))
    dense5 = tf.nn.softsign(tf.matmul(dense4, W5) + b5)

    param_list = [W1, W2, W3, W4, W5, b4, b5]

    saver = tf.train.Saver(param_list)

    return dense5, saver


def _classification(hypothesis):
    # hypothesis = dense5
    p = tf.nn.softmax(hypothesis)

    h_predict = tf.argmax(p, 1)

    return h_predict


def _CNN_main(USER_DATA_PATH, column_idx=0):
    _start_msg()
    list_time_series = pre._reader(USER_DATA_PATH, column_idx)
    time_series = pre._resize(list_time_series)
    time_series = time_series.reshape(-1, myConfig.INPUT_SIZE, 1)

    X = tf.placeholder(tf.float32, [None, myConfig.INPUT_SIZE, 1])

    hypo, model_saver = _model(X=X)
    h_predict = _classification(hypothesis=hypo)

    """Initialize"""
    sess = tf.Session()
    sess.run(tf.global_variables_initializer())
    saver = tf.train.import_meta_graph(myConfig.CNN_SAVED_MODEL_PATH)
    saver.restore(sess, tf.train.latest_checkpoint(myConfig.CNN_CHECKPOINT_PATH))

    _load_model_msg(1)

    t_trained = sess.run([h_predict], feed_dict={X: time_series})
    print(t_trained[0])

    if t_trained[0] == 1:
        print('Non periodic')
        return False
    else:
        print('Periodic')
        return True


def _start_msg():
    print("")
    print("***********************************************************")
    print("Start Classification with Deep Learning")
    print("                                 Model name: CNN 1024")
    print("                                 Optimize func.: Adam")
    print("                                 Activation func.: softsign")
    print("                                 Learning rate: 0.0001")
    print("***********************************************************")
    print("")


def _load_model_msg(sleep_time):
    print("")
    time.sleep(sleep_time)
    print(" The classification model was successfully imported!")

# TEST
# _CNN_main("D:/DKE/data/period_classification/시연데이터/line95.csv")
