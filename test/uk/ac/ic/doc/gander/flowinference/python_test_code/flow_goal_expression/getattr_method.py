# calling the getattr method means we can't be sure which attributes it
# returned so any getattr call causes call attributes to flow to top

class A:
	def __getattr__(self, attribute_name):
		return self.__dict__[attribute_name]

a = A()
x = 42
print x # blastoff

a.m = x

print a.__getattr__("m") # flows here although we can't be sure