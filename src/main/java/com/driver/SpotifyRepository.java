package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User newUser=new User(name,mobile);
        users.add(newUser);
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist=new Artist(name);
        artists.add(newArtist);
        return newArtist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artistExists=null;
        for(Artist artist:artists)
        {
            if(artist.getName().equals(artistName))
            {
                artistExists=artist;
                break;
            }
        }
        List<Album> albumsForGivenArtist;
        if(artistExists==null)
        {
            artistExists=this.createArtist(artistName);
            albumsForGivenArtist=new ArrayList<Album>();
        }
        else
            albumsForGivenArtist=artistAlbumMap.get(artistExists);
        Album newAlbum=new Album(title);
        albums.add(newAlbum);
        albumsForGivenArtist.add(newAlbum);
        artistAlbumMap.put(artistExists,albumsForGivenArtist);
        return newAlbum;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        boolean isAlbumFound=false;
        Album albumInDatabase=null;
        for(Album album:albums)
        {
            if(album.getTitle().equals(albumName))
            {
                isAlbumFound=true;
                albumInDatabase=album;
                break;
            }
        }
        if(!isAlbumFound)
            throw new Exception("Album does not exist");
        Song newSong=new Song(title,length);
        List<Song> albumSongs=albumSongMap.getOrDefault(albumInDatabase,new ArrayList<Song>());
        albumSongs.add(newSong);
        songs.add(newSong);
        albumSongMap.put(albumInDatabase,albumSongs);
        return newSong;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
       User user=findUser(mobile);
        if(user==null)
            throw new Exception("User does not exist");
        Playlist newPlaylist=new Playlist(title);
        List<Song> songsHavingGivenLength=new ArrayList<>();
        findSongsHavingGivenLength(length,songsHavingGivenLength);
        playlistSongMap.put(newPlaylist,songsHavingGivenLength);
        creatorPlaylistMap.put(user,newPlaylist);
        playlistListenerMap.put(newPlaylist,new ArrayList<User>(Arrays.asList(user)));
        List<Playlist> currentUserPlaylists=userPlaylistMap.getOrDefault(user,new ArrayList<Playlist>());
        currentUserPlaylists.add(newPlaylist);
        userPlaylistMap.put(user,currentUserPlaylists);
        return newPlaylist;
    }

    public User findUser(String mobile){
        User userInDatabase=null;
        for(User user:users)
        {
            if(user.getMobile().equals(mobile))
            {
                userInDatabase=user;
                break;
            }
        }
        return userInDatabase;
    }
    public List<Song> findSongsHavingGivenLength(int length,List<Song> songsHavingGivenLength){
        for(Song song:songs){
            if(song.getLength()==length)
                songsHavingGivenLength.add(song);
        }
        return songsHavingGivenLength;
    }
    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user=findUser(mobile);
        if(user==null)
            throw new Exception("User does not exist");
        Playlist newPlaylist=new Playlist(title);
        List<Song> songsHavingGivenTitle=new ArrayList<>();
        findSongsHavingGivenTitle(songTitles,songsHavingGivenTitle);
        playlistSongMap.put(newPlaylist,songsHavingGivenTitle);
        creatorPlaylistMap.put(user,newPlaylist);
        playlistListenerMap.put(newPlaylist,new ArrayList<>(Arrays.asList(user)));
        List<Playlist> currentUserPlaylists=userPlaylistMap.getOrDefault(user,new ArrayList<>());
        currentUserPlaylists.add(newPlaylist);
        userPlaylistMap.put(user,currentUserPlaylists);
        return newPlaylist;
    }

    public List<Song> findSongsHavingGivenTitle(List<String> songTitles,List<Song> songsHavingGivenTitle){
        for(Song song:songs)
        {
            for(String songTitle:songTitles)
            {
                if(song.getTitle().equals(songTitle))
                {
                    songsHavingGivenTitle.add(song);
                    break;
                }
            }

        }
        return songsHavingGivenTitle;
    }
    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user=findUser(mobile);
        if(user==null)
            throw new Exception("User does not exist");
        Playlist playlist=findPlaylistInDatabase(playlistTitle);
        if(playlist==null)
            throw new Exception("Playlist does not exist");
        List<User> playlistListeners=playlistListenerMap.getOrDefault(playlist,new ArrayList<>());
        if(creatorPlaylistMap.containsKey(user) && creatorPlaylistMap.get(user)==playlist || playlistListeners.contains(user))
            return playlist;
       playlistListeners.add(user);
       playlistListenerMap.put(playlist,playlistListeners);
       return playlist;
    }

    public Playlist findPlaylistInDatabase(String playlistTitle){
        Playlist playlistInDatabse=null;
        for(Playlist playlist:playlists)
        {
            if(playlist.getTitle().equals(playlistTitle))
            {
                playlistInDatabse=playlist;
                break;
            }
        }
        return playlistInDatabse;
    }
    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user=findUser(mobile);
        if(user==null)
            throw new Exception("User does not exist");
        Song song=findSong(songTitle);
        if(song == null)
            throw new Exception("Song does not exist");
        if(songLikeMap.getOrDefault(song,new ArrayList<>()).contains(user))
            return song;
        song.setLikes(song.getLikes()+1);
        songLikeMap.get(song).add(user);
        for(Album album:albumSongMap.keySet())
        {
            if(albumSongMap.get(album).contains(song))
            {
                for(Artist artist:artistAlbumMap.keySet())
                {
                    if(artistAlbumMap.get(artist).contains(album))
                    {
                        artist.setLikes(artist.getLikes()+1);
                        break;
                    }
                }
                break;
            }
        }
        return song;
    }

    public Song findSong(String title){
        Song songExists=null;
        for(Song song:songs)
        {
            if(song.getTitle().equals(title))
            {
                songExists=song;
                break;
            }
        }
        return songExists;
    }
    public String mostPopularArtist() {
        int mostLikes=Integer.MIN_VALUE;
        String famousArtist="";
        for(Artist artist:artists)
        {
            if(artist.getLikes() > mostLikes)
            {
                famousArtist=artist.getName();
                mostLikes=artist.getLikes();
            }
        }
        return famousArtist;

    }

    public String mostPopularSong() {
        int mostLikes=Integer.MIN_VALUE;
        String famousSong="";
        for(Song song:songs)
        {
            if(song.getLikes() > mostLikes)
            {
                famousSong=song.getTitle();
                mostLikes=song.getLikes();
            }
        }
        return famousSong;

    }
}
