# Class scope attribute whose only definition appears outside the class body
#
# This situation requires special care.  Any variable 'i' appearing in the
# class's scope could be bound to both 'i' in the global namespace and the
# class's attribute 'i' that gets set externally.  The attribute 'A.i', however,
# will only ever refer to the class attribute. Its type must not be polluted
# by the global variable.  This means attribute lookup must not be delegated to
# variable lookup; they work differently.

i = 99 # just to distract

class A:
	def f(self):
		print self.i # what_am_i_via_self
	
	print i # what_am_i_inside

A.i = "Hello"
A.j = [] # makes sure we're paying attention to attribute name

print A.i # what_am_i

A().f()