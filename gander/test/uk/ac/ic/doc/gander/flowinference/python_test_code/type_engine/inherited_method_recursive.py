# A flow-insensitive analysis of this program makes it seem that the 2nd class
# definition might inherit from itself.  Obviously this is not the case, but
# the analysis needs to be protected against this.

class A:
    print "I'm about to get replaced"      # first A
    
    def m(self):
		print "I am the first A's m"       # first A's m

old_A = A

class A(old_A):
    print "I am the younger, slimmer A"    # second A

a = A()

print A.m # what_am_i_unbound
print a.m # what_am_i_bound
