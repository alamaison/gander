# variables in a class aren't actually lexically bound which is quite nasty

x = 42
y = []

class A:
    x = x

print A.x # this came from the global x

# just to check that the globals don't enter the class namespace unless they
# are explicitly assigned
print A.y # this didn't come from anywhere