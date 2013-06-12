# calling the getattr function means we can't be sure which attributes it
# returned so any getattr call causes call attributes to flow to top

class A:
	pass

a = A()
x = 42
print x # blastoff

a.m = x

print getattr(a, "m") # flows here although we can't be sure