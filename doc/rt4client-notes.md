The game is implemented as an Applet, meaning that its start point - client.java - must implement the Applet life cycle methods, as well as any desired window and input events, through standardized interface methods. In a nutshell, Applets run in three phases: startup, run loop, and shutdown.

The run loop is implemented in GameShell.java, where it runs code in an infinite loop until a shutdown request is received. In each loop, it executes one game tick and one render tick, both of which are implemented in client.java.

### Initialization

Initialization of the main game logic occurs in the mainInit() method in client.java. This method initializes network and file cache queues, default gameplay settings, and keyboard and mouse event hooks. It also initializes the cache files.

### Game Loop

Each tick of the game loop occurs in the mainLoop() method in client.java. This method manages multiple looping components independently, such as the music player, keyboard input events, and timers.

### Render Loop

Each tick of the render loop occurs in the mainRedraw() method in client.java. This method's behavior varies wildly depending on the current game state, most of which are not yet known. However, two game states appear to lead directly into the main game as we know it:
- 10, which routes through InterfaceList
- 30, which routes through LoginManager

Both of the above routes pass control over to Cs1ScriptRunner.java. This class is responsible for rendering Component objects. It seems to be responsible for managing the rendering of all the layered interfaces that make up the game viewport. At various points, it will call rendering code in ScriptRunner, MiniMenu, WorldMap, InterfaceList, and so on, based on the current state of the game. This logic is complex, but the rendering of the 3D game world occurs in the renderComponent() method, and is mostly delegated to the SceneGraph class.

The SceneGraph is responsible for managing the 3D scene graphics. A scene is composed of tiles, which compose the floor, walls, and non-interactive decorative elements, and entities, which include the player, NPCs, doors, resource hotspots, examinable decorations, and certain unwalkable wall elements. Tiles are stored in a stack, and rendered in stack order. Entities are rendered after tiles, although some tiles are re-rendered after entities if they overlap.

Tiles may be one of the following:
- Plain Tile (floor tiles with flat surfaces and solid or gradient coloration)
- Shaped Tile (floor tiles with non-flat surfaces, e.g. hills and rounded areas, or with rounded edges, e.g. near rounded walls and paths with rounded edges)
- Wall
- Wall Decor
- Ground Decor (decorative elements on floor tiles, e.g. grass and small rocks)
- Stack Entity (???)

Rendering of both tiles and entities is performed by the Rasteriser class, which contains logic for rasterizing 3D triangles with textures and gradients.

### Networking and Caching

The game client cannot run properly on its own - it must download some content from the corresponding server application. This data is fetched from the server, cached on disk, and read from the cache as needed.

All requests to the server must go through a single queue, called the Js5NetQueue, and all interactions with the cache must go through a separate queue, called the Js5CacheQueue. These two queues are managed by a class called the Js5MasterIndex.

The Js5NetQueue accepts requests of type Js5NetRequest, and, in a separate thread, loops through each request in order - urgent first, then low priority - until the game is closed. Where applicable, responses from the server may be encrypted, in which case they are decrypted by ORing each byte of the response with the encryption key. Each request received is processed in the following sequence:
- A pre-request is transmitted to the server:
  - Unused (7 bits - always zero)
  - Is Urgent (1 bit)
  - Request Key (3 bytes)
- Pre-request response is read from the server:
  - Request Key (3 bytes)
  - Is Prefetch (1 bit)
  - ??? (7 bits)
  - Content Length (4 bytes)
- Full response is read from the server, in blocks of 512 bytes:
  - Unused (1 bit - always zero)
  - ??? (7 bits - copied from pre-request response)
  - Content Length (4 bytes - copied from pre-request response)
  - Unused (3 bytes)
  - Data (variable length - Content Length + Padding (??? == 0 ? 5 : 9) + Trailer Length)
- Request is marked as done and removed from queue.

Some network responses are compressed, presumably to save bandwidth during download and/or improve download speed. The compressed data file is structured as follows:
- Compression type (1 byte)
  - 0 = No compression
  - 1 = Bzip2
  - 2+ = Gzip
- Compressed data length (4 bytes, DCBA - only present if compressed)
- Uncompressed data length (4 bytes, DCBA)
- Compressed data (remainder of file)

By contrast to the Js5NetQueue, requests to interact with the cache files are managed by the Js5CacheQueue. This queue has read() and write() methods which create Js5CacheRequest objects, which are then dequeued and processed asynchronously in its Runnable.run() implementation. The Js5CacheQueue interacts with the cache, which is actually several different files. Generally, the cache can be understood as index files and data files. Index files contain metadata about where data is stored within the much larger data file. See the Data Format section for more details.

TODO: At what point are network requests cached into a file?
TODO: Where does group id come from when executing a read?
TODO: Is the data returned by the network queue and the cache queue structured the same?

### Data Format

Once downloaded from the server and cached, the data is stored in the following files:
- Master index file (main_file_cache.idx255)
- 29 index files (main_file_cache.idx0 to main_file_cache.idx28)
- Data file (main_file_cache.dat2)
- UID file (random.dat)

The master index file is fetched directly from the server on client initialization. It is used to verify any existing index files already downloaded by a previous run of the client, and download them from the server if any are missing or need updates. The master index file is composed of a 5-byte header followed by multiple 8-byte segments, one for each archive.
- Header
  - ??? (5 bytes)
- Archive
  - Checksum (4 bytes, DCBA - checksum of the raw index file data received from the server)
  - Version (4 bytes)

Broadly, the data is structured as follows:
- Each of the 29 index files corresponds to an archive, which represents a type of data. The number at the end of the .idx file extension is the archive id.
- Each archive contains some number of groups, each representing a single unit of that data.
- Each group contains some number of files, each representing a subset of the data composing the group.

Each index file is composed of a series of 6-byte segments. Each segment corresponds to a group, and the id of each group is inferred based on the order that it appears in the file (e.g. group id 2 is located at offset 0x0C, or 2 * 6 bytes). Index file segments are formatted as follows:
- Data Size (3 bytes, ABC)
- Data Index (3 bytes, ABC)

The data file is composed of a series of 520-byte (or smaller) segments. The Data Index field in the index file points to one of these segments, which can be addressed by computing (data index * 520). Each segment is a node within a linked list of multiple segments, where each segment points to the index of the next segment. Data file segments are formatted as follows:
- Group ID (2 bytes, AB)
- Order (2 bytes, BA - should increment by 1 for each segment in the linked list)
- Next Segment Index (3 bytes, CBA - value of 0 indicates end of data)
- Archive ID (1 byte)
- Data (1-512 bytes - should match Data Size in index file entry)

The data retrieved from the data file in this way is not directly usable - it is packed into a RAID-like format, either for parallelization or obfuscation reasons. All files in the group are present in the packed data, but each file is broken up and spread out across the data in a series of "stripes". The packed data is formatted as follows:
- List of data files (variable length each)
  - Size of each file is specified by stripes, described below
- List of stripes (4 bytes each, DCBA)
  - Each file has X stripes, as defined by number of stripes per file below
  - Assuming files A, B, and C, and 2 stripes per file, then format is AAAABBBBCCCCAAAABBBBCCCC
  - All stripes for each file are summed up to the total unpacked size of the file
- Number of stripes per file (1 byte)

TODO: Revisit this, I have a feeling you interpreted this incorrectly...

Once unpacked, the data file format varies depending on which archive it is in.

Archive id 0 corresponds to animations. In this archive, group id corresponds to an animation id. A single animation id corresponds to multiple files, each of which represents a single frame of animation. Each file is formatted as follows:
- Base id (2 bytes, AB - refers to bases archive)
- Header
  - Header length (1 byte)
  - List of attributes (1 byte each; 1 per model in the frame)
    - Bits 1-3 -> Not used
    - Bits 4-5 -> Flags
    - Bit 6 -> 0 to use default value for Z; 1 to use value in body
    - Bit 7 -> 0 to use default value for Y; 1 to use value in body
    - Bit 8 -> 0 to use default value for X; 1 to use value in body
- Body
  - List of X/Y/Z values as specified in header (1-2 bytes each; 1-3 per model in the frame)
    - Each trio of values defines a 3D transformation of a specific model
    - The specific transformation applied depends on the base for the file
      - Translate, rotate, scale, alpha, and color transforms appear to be possible
      - These simple transforms, when applied to multiple static models, give the illusion of complex animation of a multi-model character or entity
    - Sort order is X, then Y, then Z
    - If corresponding header attribute is 0, then value in body is omitted, rather than skipped
    - Size of value varies depending on "smart" algorithm:
      - Read 1 byte
      - If highest bit is not set, length is 1 byte
      - Otherwise, length is 2 bytes
      - In either case, drop high 2 bits

TODO: What are Flags used for?

Archive id 1 corresponds to bases. In this archive, group id corresponds to a base id. Each base has only one file, which is referenced by one or more frames of animation. Each base's file is formatted as follows:
- Num Transforms (1 byte)
- Types (1 byte each; 1 type per transform)
- Shadows (1 byte each; 1 shadow per transform)
- Parts (2 bytes each, BA; 1 part per transform)
- Num Bones (1 byte each; 1 set of bones per transform)
- Bones (1 byte each; X bones per transform, where X is Num Bones)
TODO: What do these values mean?

Archive id 7 corresponds to models. In this archive, group id corresponds to a model id. Each model has only one file. 

Models are composed of four components: vertices, triangles, textures, and particle effects.
- Vertices are points in 3D space. Each vertex is stored in the form of differentials DX, DY, and DZ, which represent the difference in position between that vertex and the previous vertex.
- Triangles are the basic polygons that make up the outer surface of the model. Each triangle is represented by its three points, A, B, and C, and are stored in the form of differentials between the points on the current triangle and the previous triangle, with the possibility of some vertices or edges being shared.
- Textures describe the surface appearance of a particular triangle face. Textures are represented in a PMN format, with the possibility of scale and rotation transformations.
- Particle effects are effects rendered around the model as a simulation of multiple particles in space. Particle effects are composed of emitters - defining where and how particles are created and their initial velocities - and effectors - defining the physics forces imposed upon the particles after they are generated. Particle effects are not in use as of the 530 build of 2009scape, and are probably only present as a work in progress feature for future releases.

TODO: Are the points of the triangles coordinates in space? Or do they refer to vertices?
TODO: What happens if you wipe all the vertices and only use faces?
TODO: What does PMN mean?

There are two types of model formats: new and old. The format for new models is built off of the format for old models, but with some changes, namely the addition of complex textures. New models are easily identifiable as their final two bytes are 0xFFFF. For simplicity, only the new model format is documented in full; for old model format, please note the following changes:
- No Texture Types - all textures are Simple.
- Triangle Info is moved to after Triangle Bones.
- Triangle Textures, Triangle Texture Index, and Triangle Texture Data Length are removed.
- DX, DY, and DZ are moved to after Simple PMN.
- Complex PMN, Complex Scale/Rotation, and Cube 1/2 are removed.
- Particle Emitters and Particle Effectors are removed.
- Has Info only contains the Has Triangle Info bit.
- New Model Indicator is not present - file simply ends.

New models are formatted as follows:
- Texture Types (1 byte each; 1 per texture (see Texture Count))
  - 0 -> Simple
  - 1 -> Complex
  - 2 -> Cube (Complex)
  - 3 -> Complex
- Flags (1 byte each; 1 per vertex (see Vertex Count))
  - Bit 1 -> If set, DX is nonzero
  - Bit 2 -> If set, DY is nonzero
  - Bit 3 -> If set, DZ is nonzero
- Triangle Info (1 byte each; 1 per triangle - only present if Has Triangle Info is true)
- Triangle Types (1 byte each; 1 per triangle)
  - This value determines how the values in Vertex Index are rendered relative to one another.
  - Type 1 pulls 3 values from the Vertex Index section, and all other types pull 1 value.
  - 1 -> 3-point diff with no shared vertices or edges
  - 2 -> 1-point diff with shared vertex A and shared edge AC-AB
  - 3 -> 1-point diff with shared vertex B and shared edge BC-AC
  - 4 -> 1-point diff with no shared vertices and shared edge AB-AB
- Triangle Priority (1 byte each; 1 per triangle - only present if Priority is 0xFF)
- Triangle Bones (1 byte each; 1 per triangle - only present if Has Triangle Bones is true)
- Vertex Bones (1 byte each; 1 per vertex - only present if Has Vertex Bones is true)
- Triangle Alpha (1 byte each; 1 per triangle - only present if Has Alpha is true)
- Vertex Index (Size specified by Vertex Index Data Length)
  - Size of value varies depending on "smart" algorithm:
    - Read 1 byte
    - If highest bit is not set, length is 1 byte
    - Otherwise, length is 2 bytes
    - In either case, drop high 2 bits
  - Each value corresponds to a diff between the current triangle and the previous triangle in sequence.
- Triangle Textures (2 bytes each; 1 per triangle - only present if Has Textures is true)
  - Subtract 1 from value before using.
- Triangle Texture Index (Size specified by Triangle Texture Data Length)
  - Subtract 1 from value before using.
  - If Triangle Textures value is -1 (after subtraction), then use value -1 and omit from file rather than skipping.
- Triangle Colors (2 bytes each; 1 per triangle)
- DX (Size specified by DX Data Length)
- DY (Size specified by DY Data Length)
- DZ (Size specified by DZ Data Length)
  - DX, DY, and DZ specify the change in X, Y, and Z, respectively, between one vertex and the next in sequence.
  - Absolute vertex positions are calculated based on the relative positions in the file.
  - If corresponding flag in Flags is not set, then diff of 0 is used (no change from previous), and entry in file is omitted rather than skipped.
- Simple PMN (6 bytes each; 1 per texture of type Simple)
- Complex PMN (6 bytes each; 1 per texture of type Complex)
- Complex Scale (6 bytes each; 1 per texture of type Complex)
- Complex Rotation (1 byte each; 1 per texture of type Complex)
- Cube 1 (1 byte each; 1 per texture of type Complex)
- Cube 2 (1-3 bytes each; 1 per texture of type Complex)
  - Textures of type Cube are 3 bytes, and other texture types are 1 byte
- Particle Emitters
  - Num Emitters (1 byte)
  - Emitters (4 bytes each; not used)
- Particle Effectors
  - Num Effectors (1 byte)
  - Effectors (4 bytes each; not used)
- Possible buffer space of unknown length
- Vertex Count (2 bytes, BA)
- Triangle Count (2 bytes, BA)
- Texture Count (1 byte)
- Has Info (1 byte)
  - Bit 1 -> Has Triangle Info
  - Bit 2 -> Has Particle Emitters
- Priority (1 byte)
- Has Alpha (1 byte)
- Has Triangle Bones (1 byte)
- Has Textures (1 byte)
- Has Vertex Bones (1 byte)
- DX Data Length (2 bytes, BA)
- DY Data Length (2 bytes, BA)
- DZ Data Length (2 bytes, BA)
- Vertex Index Data Length (2 bytes, BA)
- Triangle Texture Data Length (2 bytes, BA)
- New Model Indicator (2 bytes, always 0xFFFF)

Archive id 19

Archive id 29 does not appear to be used anywhere, so it is unclear what data this file contains.

TODO: What do the other archives store?

TODO: Where does this fit in with the data formats earlier?
