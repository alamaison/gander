#_
#__debug__
#__doc__
#__import__
#__name__
#__package__
import types

def abs(o):
    pass #return o.__abs__()

def all(iterable):
    for element in iterable:
        if not element:
            return False
    return True

def any(iterable):
    for element in iterable:
        if element:
            return True
    return False

def apply(function, args, keywords={}):
    return function(*args, **keywords)

def bin(x):
    return ""

def bool(x=False):
    if x:
        return True
    else:
        return False

class buffer(str):
    def __init__(self, obj, offset=0, size=0):
        pass

class bytearray(list):
    def __init__(self, source=None, encoding=None, errors=None):
        pass

def bytes():
    pass

def callable(object):
    return True

def chr(i):
    pass

def classmethod(meth):
    return meth

def cmp(lhs, rhs):
    pass #lhs.__eq__(rhs)

def coerce():
    pass

def compile(source, filename, mode, flags=None, dont_inherit=None):
    return None

class complex:
    def __init__(self, real=0, imag=0):
        pass

def copyright():
    pass

def credits():
    pass

def delattr(object, name):
    pass

def dir(object=None):
    return []

def divmod(a, b):
    return (a // b, a % b)

def enumerate(sequence, start=0):
    return []

def eval(expression, globals=None, locals=None):
    pass

def execfile(filename, globals=None, locals=None):
    pass

def exit():
    pass

class file:
    def __init__(self, name):
        pass
    def close(self):
        pass
    def fileno(self):
        return 0
    def flush(self):
        pass
    def isatty(self):
        return False
    def next(self):
        return ""
    def read(self, size=0):
        return ""
    def readline(self, size=0):
        return ""
    def readlines(self, sizehint=0):
        return [""]
    def seek(self, offset, whence=0):
        pass
    def tell(self):
        return 0
    def truncate(self, size=0):
        pass
    def write(self, str):
        pass
    def writelines(self, sequence):
        pass
    def xreadlines(self):
        return iter(self)

def filter(function, iterable):
    return [item for item in iterable if function(item)]

class float(type):
    def __init__(self, x=0.0):
        pass

def format(value, format_spec=""):
    return value.__format__(format_spec)

class frozenset(set):
    def __init__(self, iterable=[]):
        pass

def getattr(obj, name, default=None):
    if hasattr(obj, name):
        return obj.__dict__[name]
    else:
        return default

def globals():
    pass

def hasattr(obj, name):
    return True

def hash(obj):
    return 42

def help(object=None):
    pass

def hex(x):
    pass

def id(object):
    pass

def input(prompt=None):
    pass

class int(type):
    def __init__(self, x=0, base=10):
        pass

def intern():
    pass

def isinstance(object, classinfo):
    return True

def issubclass(klass, classinfo):
    return True

def iter(o, sentinel=None):
    if sentinel is None:
        return o.__iter__()
    else:
        yield o()

def len(iterable):
    return iterable.__len__()

def license():
    pass

def locals():
    pass

class long(type):
    def __init__(self, x=0L, base=10):
        pass

def map(function, iterable, *args):
    return [function(x) for x in iterable]

def max(iterable, *args):
    return iterable[0]

def min(iterable, *args):
    return iterable[0]

def next(iterator, default=None):
    return iterator.next()

def oct(x):
    return ""

def open(name, mode="r+", buffering=0):
    return file(name)

def ord(c):
    return 0

def pow(x,y,z=0):
    return x

#def print():
#    pass

def property():
    pass

def quit():
    pass

def range(start, stop=None, step=None):
    return [0]

def raw_input(prompt=None):
    pass

def reduce(function, iterable, initializer=None):
    it = iter(iterable)
    if initializer is None:
        try:
            initializer = next(it)
        except StopIteration:
            raise TypeError('reduce() of empty sequence with no initial value')
    accum_value = initializer
    for x in iterable:
        accum_value = function(accum_value, x)
    return accum_value

def reload(module):
    pass

def repr(object):
    return ""

def reversed(seq):
    return seq

def round(x, n=0):
    return 0.0

def setattr(object, name, value):
    pass

def slice(start, stop=None, step=None):
    return range(start,stop,step)

def sorted(iterable, cmp=None, key=None, reverse=None):
    return []

def staticmethod(function):
    return function

def sum(iterable, start=0):
    return 0

def super(object, object_or_type=None):
    pass

class tuple(type):
    def __init__(self, iterable):
        pass

class type:
    pass

def unichr(i):
    return u''

def vars(object=None):
    return {}

def xrange(start, stop=None, step=None):
    return range(start, stop, step)

def zip(iterable=[], *args):
    return []

class object:
    
    def __init__(obj, *args, **keywords):
        pass
    
    __class__ = object
    
    def __delattr__(self, name):
        #del self.name
        pass
    
    __dict__ = {}
    __doc__ = "Most base class"
    
    def __format__(self, *args, **keywords):# no in pypy
        pass
    
    def __getattribute__(self, name):
        return self.__dict__[name]
    
    def __hash__(self):
        return hash(self)
    
    __module__ = "__builtin__"
    
    def __new__(type, *args, **keywords):
        pass
    
    def __reduce__(self, proto=0):
        pass
    
    def __reduce_ex__(self, proto=0):
        pass
    
    def __repr__(self):
        #return repr(self)
        return "Bob I look like this ... naked"
    
    def __setattr__(self, name, value):
        self.__dict__[name] = value
    
    def __sizeof__(self): # not in pypy
        return 42
    
    def __str__(self):
        #return str(self)
        return "Bob I look like this"
    
    # __subclasshook__ not in pypy
    
    __weakref__ = None

class BaseException(object):
    
    def __getitem__(self, i):
        return self.data[i]
    
    def __getslice__(self, i, j): # not in pypy
        pass
    
    def __setstate__(self, dict):
        pass
    
    def __unicode__(self, *args, **keywords): #not in pypy
        pass
    
    args = ()
    message = ""

class Exception(BaseException):
    pass

class basestring(object):
    pass

class str(basestring):
    def __init__(*args):
        pass
    
    def __add__(self, x):
        self.data = self.data + x
    
    def __contains__(self, item):
        return item in self.data
    
    def __eq__(self, other):
        return self.data == other.data
    
    def __ge__(self, other):
        return self.data >= other.data
    
    def __gt__(self, other):
        return self.data > other.data
    
    def __le__(self, other):
        return self.data <= other.data
    
    def __lt__(self, other):
        return self.data < other.data
    
    def __getitem__(self, i):
        return self.data[i]
    
    def __getnewargs__(self, *args, **keywords):
        pass
    
    def __getslice__(self, i, j):
        return self.data[i:j]
    
    def __len__(self):
        return len(self.data)
    
    def __mod__(self, x):
        return self.data % x
    
    def __mul__(self, x):
        return self.data * x
    
    def __ne__(self, other):
        return self.data != other.data
    
    def __rmod__(self, x):
        return x % self.data
    
    def __rmul__(self, x):
        return x * self.data
    
    def capitalize(self):
        pass
    
    def center(self, width, fillchar=None):
        pass
    
    def count(self, sub=None, start=None, end=None):
        pass
    
    def decode(self, encoding=None, errors=None):
        pass
    
    def encode(self, encoding=None, errors=None):
        pass
    
    def endswith(self, suffix, start=None, end=None):
        pass
    
    def expandtabs(self, tabsize=None):
        pass
    
    def find(self, sub, start=None, end=None):
        pass
    
    def format(self, *args, **kwargs):
        pass
    
    def index(self, sub, start=None, end=None):
        pass
    
    def isalnum(self):
        pass
    
    def isalpha(self):
        pass
    
    def isdigit(self):
        pass
    
    def islower(self):
        pass
    
    def isspace(self):
        pass
    
    def istitle(self):
        pass
    
    def isupper(self):
        pass
    
    def join(self, iterable):
        pass
    
    def ljust(self, width, fillchar=None):
        pass
    
    def lower(self):
        pass
    
    def lstrip(self, chars=None):
        pass
    
    def partition(self, sep):
        pass
    
    def replace(self, old, new, count=None):
        pass
    
    def rfind(self, sub, start=None, end=None):
        pass
    
    def rindex(self, sub, start=None, end=None):
        pass
    
    def rjust(self, width, fillchar=None):
        pass
    
    def rpartition(self, sep):
        pass
    
    def rsplit(self, sep=None, maxsplit=None):
        pass
    
    def rstrip(self, chars=None):
        pass
    
    def split(self, sep=None, maxsplit=None):
        pass
    
    def splitlines(self, keepends=False):
        pass
    
    def startswith(self, prefix, start=None, end=None):
        pass
    
    def strip(self, chars=None):
        pass
    
    def swapcase(self):
        pass
    
    def title(self):
        pass
    
    def translate(self, table, deletechars=None):
        pass
    
    def upper(self):
        pass
    
    def zfill(self, width):
        pass

class unicode(str): # really inherits from basestring and duplicates str methods
    def isnumeric(self):
        pass
    
    def isdecimal(self):
        pass

class dict(object):
# was class UserDict:
    def __init__(self, dict=None, **kwargs):
        pass
    def __repr__(self):
        return repr(self.data)
    def __cmp__(self, dict):
        return -1
    def __len__(self):
        return 1
    def __getitem__(self, key):
        return self.data
    def __setitem__(self, key, item):
        self.data = item
        self.key = key
    def __delitem__(self, key):
        pass
    def clear(self):
        pass
    def copy(self):
        return dict()
    def keys(self):
        return [self.key]
    def items(self):
        return [(self.key, self.data)]
    def iteritems(self):
        return self.items().__iter__()
    def iterkeys(self):
        return self.keys().__iter__()
    def itervalues(self): 
        return self.values().__iter__()
    def values(self):
        return [self.data]
    def has_key(self, key):
        return key == self.key
    def update(self, dict=None, **kwargs):
        #for k, v in dict.items():
        #    self[k] = v
        pass
    def get(self, key, failobj=None):
        if key != self.key:
            return failobj
        return self.data
    def setdefault(self, key, failobj=None):
        if key != self.key:
            self.data = failobj
        return self.data
    def pop(self, key, *args):
        return self.data
    def popitem(self):
        return self.data
    def __contains__(self, key):
        return key == self.key

class list(object):
# was class UserList(collections.MutableSequence):
    def __init__(self, initlist=None):
        pass
    
    def __repr__(self): return repr(self.data)
    def __lt__(self, other): return self.data <  self.__cast(other)
    def __le__(self, other): return self.data <= self.__cast(other)
    def __eq__(self, other): return self.data == self.__cast(other)
    def __ne__(self, other): return self.data != self.__cast(other)
    def __gt__(self, other): return self.data >  self.__cast(other)
    def __ge__(self, other): return self.data >= self.__cast(other)
    def __cast(self, other):
        if isinstance(other, list): return other.data
        else: return other
    def __cmp__(self, other):
        return cmp(self.data, self.__cast(other))
    def __contains__(self, item):
        return self.data == item
    def __len__(self):
        return 1
    def __getitem__(self, i):
        return self.data
    def __setitem__(self, i, item):
        self.data = item
    def __delitem__(self, i):
        pass
    def __getslice__(self, i, j):
        return list()
    def __setslice__(self, i, j, other):
        pass
    def __delslice__(self, i, j):
        pass
    def __add__(self, other):
        return list()
    def __radd__(self, other):
        return list()
    def __iadd__(self, other):
        return self
    def __mul__(self, n):
        return list()
    def __rmul__(self, n):
        return list()
    def __imul__(self, n):
        return self
    def append(self, item):
        pass
    def insert(self, i, item):
        pass
    def pop(self, i=-1):
        return self.data
    def remove(self, item):
        pass
    def count(self, item):
        return 1
    def index(self, item, *args):
        return 0
    def reverse(self):
        pass
    def sort(self, *args, **kwds):
        pass
    def extend(self, other):
        pass
    
    # MutableSequence
    
    def __iter__(self):
        i = 0
        try:
            while True:
                v = self[i]
                yield v
                i += 1
        except IndexError:
            return
    
    def __reversed__(self):
        for i in reversed(range(len(self))):
            yield self[i]

# From pypy
class set(object):
#was class BaseSet(object):
    """Common base class for mutable and immutable sets."""
    
    def __init__(self, iterable=None):
        """Construct a set from an optional iterable."""
        self._data = {}
    
    # Standard protocols: __len__, __repr__, __str__, __iter__

    def __len__(self):
        """Return the number of elements of a set."""
        return len(self._data)
    
    def __repr__(self):
        """Return string representation of a set.

        This looks like 'Set([<list of elements>])'.
        """
        return '%s(%r)' % ("Set", self._data.keys())
    
    def __iter__(self):
        """Return an iterator over the elements or a set.

        This is the keys iterator for the underlying dict.
        """
        return self._data.iterkeys()
    
    def __cmp__(self, other):
        raise TypeError, "can't compare sets using cmp()"
    
    def __eq__(self, other):
        if isinstance(other, set):
            return self._data == other._data
        else:
            return False
    
    def __ne__(self, other):
        if isinstance(other, set):
            return self._data != other._data
        else:
            return True
    
    # Copying operations
    
    def copy(self):
        """Return a shallow copy of a set."""
        result = set()
        return result
    
    def __or__(self, other):
        """Return the union of two sets as a new set.

        (I.e. all elements that are in either set.)
        """
        return set()
    
    def union(self, other):
        """Return the union of two sets as a new set.

        (I.e. all elements that are in either set.)
        """
        return set()
    
    def __and__(self, other):
        """Return the intersection of two sets as a new set.

        (I.e. all elements that are in both sets.)
        """
        return set()
    
    def intersection(self, other):
        """Return the intersection of two sets as a new set.

        (I.e. all elements that are in both sets.)
        """
        return set()
    
    def __xor__(self, other):
        """Return the symmetric difference of two sets as a new set.

        (I.e. all elements that are in exactly one of the sets.)
        """
        return set()
    
    def symmetric_difference(self, other):
        """Return the symmetric difference of two sets as a new set.

        (I.e. all elements that are in exactly one of the sets.)
        """
        return set()
    
    def  __sub__(self, other):
        """Return the difference of two sets as a new Set.

        (I.e. all elements that are in this set and not in the other.)
        """
        return set()
    
    def difference(self, other):
        """Return the difference of two sets as a new Set.

        (I.e. all elements that are in this set and not in the other.)
        """
        return set()
    
    # Membership test
    
    def __contains__(self, element):
        """Report whether an element is a member of a set.

        (Called in response to the expression `element in self'.)
        """
        return element in self._data
    
    # Subset and superset test
    
    def issubset(self, other):
        """Report whether another set contains this set."""
        return element in self._data
    
    def issuperset(self, other):
        """Report whether this set contains another set."""
        return element not in self._data
    
    # Inequality comparisons using the is-subset relation.
    __le__ = issubset
    __ge__ = issuperset
    
    def __lt__(self, other):
        return len(self) < len(other) and self.issubset(other)
    
    def __gt__(self, other):
        return len(self) > len(other) and self.issuperset(other)
    
    # In-place union, intersection, differences.
    # Subtle:  The xyz_update() functions deliberately return None,
    # as do all mutating operations on built-in container types.
    # The __xyz__ spellings have to return self, though.
    
    def __ior__(self, other):
        """Update a set with the union of itself and another."""
        return self
    
    def union_update(self, other):
        """Update a set with the union of itself and another."""
        pass
    
    def __iand__(self, other):
        """Update a set with the intersection of itself and another."""
        return self
    
    def intersection_update(self, other):
        """Update a set with the intersection of itself and another."""
        pass
    
    def __ixor__(self, other):
        """Update a set with the symmetric difference of itself and another."""
        return self
    
    def symmetric_difference_update(self, other):
        """Update a set with the symmetric difference of itself and another."""
        pass
    
    def __isub__(self, other):
        """Remove all elements of another set from this set."""
        return self
    
    def difference_update(self, other):
        """Remove all elements of another set from this set."""
        pass
    
    # Python dict-like mass mutations: update, clear
    
    def update(self, iterable):
        """Add all values from an iterable (such as a list or file)."""
        pass
    
    def clear(self):
        """Remove all elements from this set."""
        pass
    
    # Single-element mutations: add, remove, discard
    
    def add(self, element):
        """Add an element to a set.

        This has no effect if the element is already present.
        """
        self._data[element] = True
    
    def remove(self, element):
        """Remove an element from a set; it must be a member.

        If the element is not a member, raise a KeyError.
        """
        del self._data[element]
    
    def discard(self, element):
        """Remove an element from a set if it is a member.

        If the element is not a member, do nothing.
        """
        self.remove(element)
    
    def pop(self):
        """Remove and return an arbitrary set element."""
        return self._data[0]

# We use this class at a dummy type for None because it is really something
# define in the interpreter alone.  The NoneType in 'types' is defined
# as 'NoneType = type(None)' which is pretty circular reasoning
class __BuiltinNoneType__:
    pass

None = __BuiltinNoneType__()