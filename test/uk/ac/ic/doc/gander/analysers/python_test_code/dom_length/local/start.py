def fun():
	x = y
	x.j() # length 4
	other(x)
	x.k() # length 4

def other(z):
	z.l() # length 2
	if z.m(): # length 2
		z.n() # length 3