package org.example.boardproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.boardproject.domain.Board;
import org.example.boardproject.service.BoardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    @GetMapping("/list")
    public String board(Model model,
                        @RequestParam(defaultValue = "1")int page,
                        @RequestParam(defaultValue = "5") int size){
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Board> board = boardService.findAllContents(pageable);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        model.addAttribute("board", board);
        model.addAttribute("currentPage", page);
        model.addAttribute("dateFormatter", formatter);
        return "list";
    }
    @GetMapping("/view")
    public String showDetail(@RequestParam(name = "id") Long id, Model model){
        Board board = boardService.findBoardById(id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm");
        model.addAttribute("board", board);
        model.addAttribute("dateFormatter", formatter);
        return "view";
    }
    @GetMapping("/writeform")
    public String writeForm(Model model){
        model.addAttribute("board", new Board());
        return "writeform";
    }
    @PostMapping("/write")
    public String write(@ModelAttribute Board board,
                        RedirectAttributes redirectAttributes){
        board.setCreated_at(LocalDateTime.now());
        board.setUpdated_at(LocalDateTime.now());
        boardService.saveBoard(board);
        redirectAttributes.addFlashAttribute("message", "글이 등록되었습니다.");
        return "redirect:/list";
    }
    @GetMapping("/deleteform")
    public String deleteForm(@RequestParam(name = "id") Long id, Model model){
        model.addAttribute("id",id);
        return "deleteform";
    }
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id,
                         @RequestParam("password") String password,
                         Model model){
        if (boardService.findBoardById(id).getPassword().equals(password)){
            boardService.deleteBoard(id);
            return "redirect:/list";
        }
        model.addAttribute("message", "password가 올바르지 않습니다.");
        return "deleteform";
    }
    @GetMapping("/updateform")
    public String updateForm(@RequestParam(name = "id") Long id, Model model){
        model.addAttribute("board", boardService.findBoardById(id));
        return "updateform";
    }
    @PostMapping("/update")
    public String update(@ModelAttribute Board board,
                        RedirectAttributes redirectAttributes){
        board.setUpdated_at(LocalDateTime.now());
        boardService.saveBoard(board);
        redirectAttributes.addFlashAttribute("message", "글이 수정되었습니다.");
        return "redirect:/view?id=" + board.getId();
    }
}
