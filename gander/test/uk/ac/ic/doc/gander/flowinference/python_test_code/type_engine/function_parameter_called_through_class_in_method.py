# functions in a class's namespace are converted into (non-static) methods when
# retrieved.  If retrieved on the class, they are unbound methods that expect
# the first argument to be an instance of the class; if retrieved via an 
# instance they are bound methods that expect to be called with one fewer
# parameter that than the function declaration as the first argument is
# automatically passed the instance.

# In this case the call to f via g is passed the X instance as a parameter
# so x's type is { str, X }

def f(x):
	print x # what_am_i

f("Hello")

class X:
	g = f
	
	def m(self):
		self.g()

x = X()
x.m()
