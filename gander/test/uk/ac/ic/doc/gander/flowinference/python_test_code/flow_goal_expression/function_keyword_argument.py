def f(a):
	print a # a in f

def g(a, b):
	print a # a in g
	print b # not b in g

def h(a, b):
	print b # b in h
	print a # nor a in h

x = 2
print x # blastoff

f(a=x)

y = 3
g(b=y, a=x)
h(y, b=x)
