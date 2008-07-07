/*
 * Copyright (c) 2002-2008, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.portal.service.filter;

import fr.paris.lutece.portal.service.util.AppLogService;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * MainFilter
 */
public class MainFilter implements Filter
{
    /**
     * {@inheritDoc}
     */
    public void init( FilterConfig config ) throws ServletException
    {
        FilterService.setServletContext( config.getServletContext() );
    }

    /**
     * {@inheritDoc}
     */
    public void doFilter( ServletRequest requestServlet, ServletResponse response, FilterChain chain )
        throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) requestServlet;

        for ( LuteceFilter filter : FilterService.getInstance(  ).getFilters(  ) )
        {
            // Catch exception for each filter to execute all chain
            try
            {
                // Checks mapping and plugin status
                if ( matchMapping( filter, request ) && filter.getPlugin(  ).isInstalled(  ) )
                {
                    filter.getFilter(  ).doFilter( request, response, chain );
                }
            }
            catch ( Exception e )
            {
                AppLogService.error( "Error execution doFilter method - Filter " + filter.getName(  ), e );
            }
        }

        chain.doFilter( request, response );
    }

    /**
     * {@inheritDoc}
     */
    public void destroy(  )
    {
        for ( LuteceFilter filter : FilterService.getInstance(  ).getFilters(  ) )
        {
            // Catch exception for each filter to execute all chain
            try
            {
                // Checks mapping and plugin status
                if ( filter.getPlugin(  ).isInstalled(  ) )
                {
                    filter.getFilter(  ).destroy(  );
                }
            }
            catch ( Exception e )
            {
                AppLogService.error( "Error execution destroy() method - Filter " + filter.getName(  ), e );
            }
        }
    }
    
    /**
     * Check the mapping of the request with an url pattern
     * @param filter The filter
     * @param request The request
     * @return True if the request match the url pattern
     */

    boolean matchMapping( LuteceFilter filter, HttpServletRequest request )
    {
        return matchFilterUrl( filter.getMappingUrlPattern(  ), request.getServletPath(  ) );
    }

    /**
     * Check the mapping of the request with an url pattern according servlet 
     * specifications 2.3 rules
     * @param strUrlPattern The filter url pattern
     * @param strRequestUrl The request Url
     * @return True if the request match the url pattern
     * 
     * Algorithm comming from tomcat6
     */

    boolean matchFilterUrl( String strUrlPattern, String strRequestUrl )
    {
        if ( strUrlPattern == null )
        {
            return ( false ); // Case 1 - Exact Match
        }

        if ( strUrlPattern.equals( strRequestUrl ) )
        {
            return ( true ); // Case 2 - Path Match ("/.../*")
        }

        if ( strUrlPattern.equals( "/*" ) )
        {
            return ( true );
        }

        if ( strUrlPattern.endsWith( "/*" ) )
        {
            if ( strUrlPattern.regionMatches( 0, strRequestUrl, 0, strUrlPattern.length(  ) - 2 ) )
            {
                if ( strRequestUrl.length(  ) == ( strUrlPattern.length(  ) - 2 ) )
                {
                    return ( true );
                }
                else if ( '/' == strRequestUrl.charAt( strUrlPattern.length(  ) - 2 ) )
                {
                    return ( true );
                }
            }

            return ( false );
        }

        // Case 3 - Extension Match
        if ( strUrlPattern.startsWith( "*." ) )
        {
            int slash = strRequestUrl.lastIndexOf( '/' );
            int period = strRequestUrl.lastIndexOf( '.' );

            if ( ( slash >= 0 ) && ( period > slash ) && ( period != ( strRequestUrl.length(  ) - 1 ) ) &&
                    ( ( strRequestUrl.length(  ) - period ) == ( strUrlPattern.length(  ) - 1 ) ) )
            {
                return ( strUrlPattern.regionMatches( 2, strRequestUrl, period + 1, strUrlPattern.length(  ) - 2 ) );
            }
        }

        // Case 4 - "Default" Match
        return ( false ); // NOTE - Not relevant for selecting filters
    }
}