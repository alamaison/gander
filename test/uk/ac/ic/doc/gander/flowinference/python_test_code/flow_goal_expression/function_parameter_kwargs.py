def f(**a):
	print a # a in f
	print a["bob"]

x = 2
y = 3

print x # blastoff

f(sally=y, bob=x)
