# functions in a class's namespace are converted into (non-static) methods when
# retrieved.  If retrieved on the class, they are unbound methods that expect
# the first argument to be an instance of the class; if retrieved via an 
# instance they are bound methods that expect to be called with one fewer
# parameter that than the function declaration as the first argument is
# automatically passed the instance.

# In this case the call to f via g is passed the X instance as a parameter
# so x's type is { str, X }

import function_parameter_called_from_other_module_through_class

class X:
	g = function_parameter_called_from_other_module_through_class.f
	
	def m(self):
		self.g()

X.g(X())

x = X()
x.m()
