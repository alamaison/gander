# The object being flowed is not imported by star but it's container is

class Bob:
	pass

from import_star_indirect import *

print A.b # imported by *
print A # imported by * but different object
print Bob # not imported by *