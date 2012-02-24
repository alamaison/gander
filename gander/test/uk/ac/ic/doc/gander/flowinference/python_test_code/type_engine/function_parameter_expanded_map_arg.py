def f(p, a, b, c):
	print p # p
	print a # a
	print b # b
	print c # c

m = {"a": "uncle", "c" : 42}

f(99, b=[], **m)