# A single element AST tuple with a trailing comma really is a tuple

x = 42
print x # blastoff

u = (x,)

print u # x does not flow here because of comma
