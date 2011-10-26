package net.imglib2.algorithm.mser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Set;

import net.imglib2.Localizable;
import net.imglib2.Location;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;

public final class ComponentTree< T extends Comparable< T > & Type< T >, C extends Component< T > >
{
	/**
	 * Iterate pixel positions in 4-neighborhood.
	 */
	private final class Neighborhood
	{
		/**
		 * index of the next neighbor to visit.
		 * 0 is pixel at x-1,
		 * 1 is pixel at x+1,
		 * 2 is pixel at y-1,
		 * 3 is pixel at y+1, and so on.
		 */
		private int n;
		
		/**
		 * number of neighbors, e.g., 4 for 2d images.
		 */
		private final int nBound;
		
		/**
		 * image dimensions. used to check out-of-bounds.
		 */
		final long[] dimensions;

		public Neighborhood( final long[] dim )
		{
			n = 0;
			nBound = dim.length * 2;
			dimensions = dim;
		}
		
		public int getNextNeighborIndex()
		{
			return n;
		}
		
		public void setNextNeighborIndex( int n )
		{
			this.n = n;
		}
		
		public void reset()
		{
			n = 0;
		}

		public boolean hasNext()
		{
			return n < nBound;
		}

		/**
		 * Set neighbor to the next (according to {@link ComponentTree.Neighborhood#n}) neighbor position of current.
		 * @param current
		 * @param neighbor
		 * @return false if the neighbor position is out of bounds, true otherwise.
		 */
		public boolean next( final Localizable current, final Positionable neighbor )
		{
			// TODO: can setting full position be avoided?
			neighbor.setPosition( current );
			final int d = n / 2;
			if ( n % 2 == 0 )
			{
				neighbor.move( -1, d );
				++n;
				return current.getLongPosition( d ) - 1 >= 0;
			}
			else
			{
				neighbor.move( 1, d );				
				++n;
				return current.getLongPosition( d ) + 1 < dimensions[ d ];
			}
		}
	}

	/**
	 * A pixel position on the heap of boundary pixels to be processed next.
	 * The heap is sorted by pixel values.
	 */
	private final class BoundaryPixel extends Location implements Comparable< BoundaryPixel >
	{
		private final T value;	

		// TODO: this should be some kind of iterator over the neighborhood
		private final int nextNeighborIndex;

		public BoundaryPixel( final Localizable position, final T value, int nextNeighborIndex )
		{
			super( position );
			this.nextNeighborIndex = nextNeighborIndex;
			this.value = value.copy();
		}

		public int getNextNeighborIndex()
		{
			return nextNeighborIndex;
		}

		public T get()
		{
			return value;
		}
		
		@Override
		public int compareTo( BoundaryPixel o )
		{
			return value.compareTo( o.value );
		}
	}

	private final Component.Generator< T, C > componentGenerator;
	
	private final Component.Handler< C > componentOutput;
 
	private final Neighborhood neighborhood;

	private final RandomAccessible< BitType > visited;

	private final RandomAccess< BitType > visitedRandomAccess;

	private final PriorityQueue< BoundaryPixel > boundaryPixels;

	private final Deque< C > componentStack;

	public ComponentTree( final RandomAccessibleInterval< T > input, final Component.Generator< T, C > componentGenerator, final Component.Handler< C > componentOutput )
	{
		this.componentGenerator = componentGenerator;
		this.componentOutput = componentOutput;

		final long[] dimensions = new long[ input.numDimensions() ];
		input.dimensions( dimensions );

		ImgFactory< BitType > imgFactory = new ArrayImgFactory< BitType >();
		visited = imgFactory.create( dimensions, new BitType() );
		visitedRandomAccess = visited.randomAccess();

		neighborhood = new Neighborhood( dimensions );

		boundaryPixels = new PriorityQueue< BoundaryPixel >();

		componentStack = new ArrayDeque< C >();
		componentStack.push( componentGenerator.createMaxComponent() );
		
		run( input );
	}
	
	private void visit( final Localizable position )
	{
		visitedRandomAccess.setPosition( position );
		visitedRandomAccess.get().set( true );
	}

	private boolean wasVisited( final Localizable position )
	{
		visitedRandomAccess.setPosition( position );
		return visitedRandomAccess.get().get();
	}
	

	private void run( final RandomAccessibleInterval< T > input )
	{
		RandomAccess< T > current = input.randomAccess();
		RandomAccess< T > neighbor = input.randomAccess();
		input.min( current );
		T currentLevel = current.get().createVariable();
		T neighborLevel = current.get().createVariable();

		// step 2
		visit( current );
		currentLevel.set( current.get() );
		
		// step 3
		componentStack.push( componentGenerator.createComponent( currentLevel ) );
		
		// step 4
		while ( true )
		{
			while ( neighborhood.hasNext() )
			{
				if ( ! neighborhood.next( current, neighbor ) )
					continue;
				if ( ! wasVisited( neighbor ) )
				{
					visit( neighbor );
					neighborLevel.set( neighbor.get() );
					if ( neighborLevel.compareTo( currentLevel ) >= 0 )
					{
						boundaryPixels.add( new BoundaryPixel( neighbor, neighborLevel, 0 ) );
					}
					else
					{
						boundaryPixels.add( new BoundaryPixel( current, currentLevel, neighborhood.getNextNeighborIndex() ) );
						current.setPosition( neighbor );
						currentLevel.set( neighborLevel );
	
						// go to 3, i.e.:
						componentStack.push( componentGenerator.createComponent( currentLevel ) );
						neighborhood.reset();
					}
				}
			}
			
			// step 5
			C component = componentStack.peek(); // TODO: no need to peek(), just keep it in local variable when it's created 
			component.addPosition( current );
			
			// step 6
			if ( boundaryPixels.isEmpty() )
			{
				processStack( currentLevel );
				System.out.println("done");
				return;
			}
			
			BoundaryPixel p = boundaryPixels.poll();
			if ( p.get().compareTo( currentLevel ) != 0 )
			{
				// step 7
				processStack( p.get() );
			}
			current.setPosition( p );
			currentLevel.set( p.get() );
			neighborhood.setNextNeighborIndex( p.getNextNeighborIndex() );
		}
	}
	
	/**
	 * This is called whenever the current value is raised.
	 * 
	 * @param value
	 */
	private void processStack( T value )
	{
		while (true)
		{
			// process component on top of stack
			C component = componentStack.pop();
			componentOutput.emit( component );
			
			// get level of second component on stack
			C secondComponent = componentStack.peek();
			final int c = value.compareTo( secondComponent.getValue() );
			if ( c < 0 )
			{
				component.setValue( value );
				componentStack.push( component );
			}
			else
			{
				secondComponent.merge( component );
				if ( c > 0 )
					continue;
			}
			return;
		}
	}
}
