def f(a):
	return a

def g(a):
	return a

h = f
print h(2) # calls f

p = g
print p(2) # doesn't call f
