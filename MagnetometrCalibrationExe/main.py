import csv
import matplotlib.pyplot as plt

X = []
Y = []
Z = []

with open('C:\\Users\\Вадим\\Desktop\\PET\\Quadrocopter\\RaspberryController\\data_for_magneto_calibtation.txt', 'r') as datafile:
    plotting = csv.reader(datafile, delimiter=' ')

    for ROWS in plotting:
        X.append(float(ROWS[0]) - 474.114891)
        Y.append(float(ROWS[1]) - (-84.097065))
        Z.append(float(ROWS[2]) - 185.820849)

fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')
ax.plot(X, Y, Z)
plt.show()
