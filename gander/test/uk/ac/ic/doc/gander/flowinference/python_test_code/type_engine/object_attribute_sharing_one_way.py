# check that sharing of object and class members only flows one way: objects
# share class member but not vice versa

class A:
	i = "Hello"

a = A()
a.i = []


print A.i # what_am_i_class

print a.i # what_am_i_instance
